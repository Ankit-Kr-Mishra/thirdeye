/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.task;

import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import ai.startree.thirdeye.spi.task.TaskInfo;
import ai.startree.thirdeye.spi.task.TaskStatus;
import ai.startree.thirdeye.spi.task.TaskType;
import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Singleton
public class TaskDriverRunnable implements Runnable {

  private static final Logger LOG = LoggerFactory.getLogger(TaskDriverRunnable.class);
  private static final Random RANDOM = new Random();
  private static final Set<TaskStatus> ALLOWED_OLD_TASK_STATUS = ImmutableSet
      .of(TaskStatus.FAILED, TaskStatus.WAITING);

  private final TaskManager taskManager;
  private final TaskContext taskContext;
  private final AtomicBoolean shutdown;
  private final ExecutorService taskExecutorService;
  private final TaskDriverConfiguration config;
  private final long workerId;
  private final TaskRunnerFactory taskRunnerFactory;
  private final Counter taskDurationCounter;
  private final Counter taskExceptionCounter;
  private final Counter taskSuccessCounter;
  private final Counter taskCounter;

  public TaskDriverRunnable(final TaskManager taskManager,
      final TaskContext taskContext,
      final AtomicBoolean shutdown,
      final ExecutorService taskExecutorService,
      final TaskDriverConfiguration config,
      final long workerId,
      final TaskRunnerFactory taskRunnerFactory,
      final MetricRegistry metricRegistry) {

    this.taskManager = taskManager;
    this.taskContext = taskContext;
    this.shutdown = shutdown;
    this.taskExecutorService = taskExecutorService;
    this.config = config;
    this.workerId = workerId;
    this.taskRunnerFactory = taskRunnerFactory;

    taskDurationCounter = metricRegistry.counter("taskDurationCounter");
    taskExceptionCounter = metricRegistry.counter("taskExceptionCounter");
    taskSuccessCounter = metricRegistry.counter("taskSuccessCounter");
    taskCounter = metricRegistry.counter("taskCounter");
  }

  public void run() {
    while (!shutdown.get()) {
      // select a task to execute, and update it to RUNNING
      final TaskDTO taskDTO = waitForTask();
      if (taskDTO == null) {
        continue;
      }

      // a task has acquired and we must finish executing it before termination
      runAcquiredTask(taskDTO);
    }
    LOG.info("Thread safely quiting");
  }

  private void runAcquiredTask(final TaskDTO taskDTO) {
    MDC.put("job.name", taskDTO.getJobName());
    LOG.info("Executing task {} {}", taskDTO.getId(), taskDTO.getTaskInfo());

    final long tStart = System.currentTimeMillis();
    taskCounter.inc();

    Future<List<TaskResult>> future = null;
    try {
      future = runTaskAsync(taskDTO);
      // wait for the future to complete
      future.get(config.getMaxTaskRunTime().toMillis(), TimeUnit.MILLISECONDS);

      LOG.info("DONE Executing task {}", taskDTO.getId());
      // update status to COMPLETED
      updateTaskStatus(taskDTO.getId(),
          TaskStatus.COMPLETED,
          "");

      taskSuccessCounter.inc();
    } catch (TimeoutException e) {
      handleTimeout(taskDTO, future, e);
    } catch (Exception e) {
      handleException(taskDTO, e);
    } finally {
      MDC.clear();
      long elapsedTime = System.currentTimeMillis() - tStart;
      LOG.info("Task {} took {}ms", taskDTO.getId(), elapsedTime);
      taskDurationCounter.inc(elapsedTime);
    }
  }

  private Future<List<TaskResult>> runTaskAsync(final TaskDTO taskDTO) throws IOException {
    final TaskType taskType = taskDTO.getTaskType();
    final TaskInfo taskInfo = TaskInfoFactory.get(taskType, taskDTO.getTaskInfo());
    final TaskRunner taskRunner = taskRunnerFactory.get(taskType);

    // execute the selected task asynchronously
    return taskExecutorService.submit(() -> taskRunner.execute(taskInfo, taskContext));
  }

  private void handleTimeout(final TaskDTO taskDTO, final Future<List<TaskResult>> future,
      final TimeoutException e) {
    taskExceptionCounter.inc();
    LOG.error("Timeout on executing task", e);
    if (future != null) {
      future.cancel(true);
      LOG.info("Executor thread gets cancelled successfully: {}", future.isCancelled());
    }

    updateTaskStatus(taskDTO.getId(),
        TaskStatus.TIMEOUT,
        e.getMessage());
  }

  private void handleException(final TaskDTO taskDTO, final Exception e) {
    taskExceptionCounter.inc();
    LOG.error("Exception in electing and executing task", e);

    // update task status failed
    updateTaskStatus(taskDTO.getId(),
        TaskStatus.FAILED,
        String.format("%s\n%s", ExceptionUtils.getMessage(e), ExceptionUtils.getStackTrace(e)));
  }

  /**
   * Returns a TaskDTO if a task is successfully acquired; returns null if system is shutting down.
   *
   * @return null if system is shutting down.
   */
  private TaskDTO waitForTask() {
    while (!shutdown.get()) {
      final List<TaskDTO> anomalyTasks = findTasks();

      final boolean tasksFound = CollectionUtils.isNotEmpty(anomalyTasks);
      if (tasksFound) {
        final TaskDTO taskDTO = acquireTask(anomalyTasks);
        if (taskDTO != null) {
          return taskDTO;
        }
      }
      sleep(!tasksFound);
    }
    return null;
  }

  private TaskDTO acquireTask(final List<TaskDTO> anomalyTasks) {
    // shuffle candidate tasks to avoid synchronized patterns across threads (and hosts)
    Collections.shuffle(anomalyTasks);

    for (final TaskDTO taskDTO : anomalyTasks) {
      try {
        // Don't acquire a new task if shutting down.
        if (!shutdown.get()) {
          boolean success = taskManager.updateStatusAndWorkerId(workerId,
              taskDTO.getId(),
              ALLOWED_OLD_TASK_STATUS,
              taskDTO.getVersion());
          if (success) {
            return taskDTO;
          }
        }
      } catch (Exception e) {
        LOG.warn("Got exception when acquiring task. (Worker Id: {})", workerId, e);
      }
    }
    return null;
  }

  private List<TaskDTO> findTasks() {
    try {
      // randomize fetching head and tail to reduce synchronized patterns across threads (and hosts)
      boolean orderAscending = System.currentTimeMillis() % 2 == 0;

      // find by task type to separate online task from a normal task
      return taskManager.findByStatusOrderByCreateTime(TaskStatus.WAITING,
          config.getTaskFetchSizeCap(),
          orderAscending);
    } catch (Exception e) {
      LOG.error("Exception found in fetching new tasks", e);
    }
    return null;
  }

  private void sleep(final boolean hasFetchError) {
    final long sleepTime = hasFetchError
        ? config.getTaskFailureDelay().toMillis()
        : config.getNoTaskDelay().toMillis() + RANDOM
            .nextInt((int) config.getRandomDelayCap().toMillis());
    // sleep for few seconds if not tasks found - avoid cpu thrashing
    // also add some extra random number of milli seconds to allow threads to start at different times
    try {
      Thread.sleep(sleepTime);
    } catch (InterruptedException e) {
      if (!shutdown.get()) {
        LOG.warn(e.getMessage(), e);
      }
    }
  }

  private void updateTaskStatus(long taskId,
      TaskStatus newStatus,
      String message) {
    try {
      taskManager.updateStatusAndTaskEndTime(taskId,
          TaskStatus.RUNNING,
          newStatus,
          System.currentTimeMillis(),
          message);
      LOG.info("Updated status to {}", newStatus);
    } catch (Exception e) {
      LOG.error(String.format(
          "Exception: updating task status. Request: taskId: %d, newStatus: %s, msg: %s",
          taskId,
          newStatus,
          message), e);
    }
  }
}