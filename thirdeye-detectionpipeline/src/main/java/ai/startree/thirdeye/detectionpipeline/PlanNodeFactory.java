/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.detectionpipeline;

import static ai.startree.thirdeye.spi.Constants.K_DATASET_MANAGER;
import static ai.startree.thirdeye.spi.Constants.K_DATA_SOURCE_CACHE;
import static ai.startree.thirdeye.spi.Constants.K_DETECTION_REGISTRY;
import static ai.startree.thirdeye.spi.Constants.K_EVENT_MANAGER;
import static ai.startree.thirdeye.spi.Constants.K_POST_PROCESSOR_REGISTRY;
import static java.util.Objects.requireNonNull;

import ai.startree.thirdeye.datasource.cache.DataSourceCache;
import ai.startree.thirdeye.detectionpipeline.plan.AnomalyDetectorPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.CombinerPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.DataFetcherPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.DelayPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EchoPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EnumeratorPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EventFetcherPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.EventTriggerPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.ForkJoinPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.IndexFillerPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.PostProcessorPlanNode;
import ai.startree.thirdeye.detectionpipeline.plan.SqlExecutionPlanNode;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class PlanNodeFactory {

  private static final Logger LOG = LoggerFactory.getLogger(PlanNodeFactory.class);

  /* List of plan node classes that are built in with thirdeye */
  private static final List<Class<? extends PlanNode>> BUILT_IN_PLAN_NODE_CLASSES = ImmutableList.of(
      AnomalyDetectorPlanNode.class,
      CombinerPlanNode.class,
      DataFetcherPlanNode.class,
      EchoPlanNode.class,
      EnumeratorPlanNode.class,
      EventTriggerPlanNode.class,
      EventFetcherPlanNode.class,
      ForkJoinPlanNode.class,
      IndexFillerPlanNode.class,
      SqlExecutionPlanNode.class,
      DelayPlanNode.class,
      PostProcessorPlanNode.class
  );
  /**
   * Contains the list of built in as well as node/operators coming from plugins.
   * TODO spyne implement loading nodes from plugins
   */
  private final Map<String, Class<? extends PlanNode>> planNodeTypeToClassMap;
  private final DataSourceCache dataSourceCache;
  private final DetectionRegistry detectionRegistry;
  private final PostProcessorRegistry postProcessorRegistry;
  private final EventManager eventManager;
  private final DatasetConfigManager datasetManager;

  @Inject
  public PlanNodeFactory(final DataSourceCache dataSourceCache,
      final DetectionRegistry detectionRegistry,
      final PostProcessorRegistry postProcessorRegistry,
      final EventManager eventManager,
      final DatasetConfigManager datasetManager) {
    this.dataSourceCache = dataSourceCache;
    this.detectionRegistry = detectionRegistry;
    this.postProcessorRegistry = postProcessorRegistry;
    this.planNodeTypeToClassMap = buildPlanNodeTypeToClassMap();
    this.eventManager = eventManager;
    this.datasetManager = datasetManager;
  }

  public static PlanNode build(
      final Class<? extends PlanNode> planNodeClass,
      final PlanNodeContext context
  ) throws ReflectiveOperationException {
    final Constructor<?> constructor = planNodeClass.getConstructor();
    final PlanNode planNode = (PlanNode) constructor.newInstance();
    planNode.init(context);
    return planNode;
  }

  private Map<String, Class<? extends PlanNode>> buildPlanNodeTypeToClassMap() {
    final HashMap<String, Class<? extends PlanNode>> stringClassHashMap = new HashMap<>();
    for (Class<? extends PlanNode> c : BUILT_IN_PLAN_NODE_CLASSES) {
      try {
        stringClassHashMap.put(c.newInstance().getType(), c);
      } catch (InstantiationException | IllegalAccessException e) {
        throw new RuntimeException("Failed to initialize PlanNode: " + c.getSimpleName(), e);
      }
    }
    return stringClassHashMap;
  }

  public PlanNode build(final PlanNodeBean planNodeBean,
      final PlanNodeContext runTimeContext,
      final Map<String, PlanNode> pipelinePlanNodes) {
    final PlanNodeContext context = PlanNodeContext.copy(runTimeContext)
        .setName(planNodeBean.getName())
        .setPlanNodeBean(planNodeBean)
        .setPipelinePlanNodes(pipelinePlanNodes)
        .setEnumerationItem(runTimeContext.getEnumerationItem())
        .setProperties(ImmutableMap.<String, Object>builder()
            .put(K_DATA_SOURCE_CACHE, dataSourceCache)
            .put(K_DETECTION_REGISTRY, detectionRegistry)
            .put(K_POST_PROCESSOR_REGISTRY, postProcessorRegistry)
            .put(K_EVENT_MANAGER, eventManager)
            .put(K_DATASET_MANAGER, datasetManager)
            .build()
        );

    final String type = requireNonNull(planNodeBean.getType(), "node type is null");
    final Class<? extends PlanNode> planNodeClass = requireNonNull(planNodeTypeToClassMap.get(type),
        "Unknown node type: " + type);
    try {
      return build(planNodeClass, context);
    } catch (final Exception e) {
      throw new IllegalArgumentException("Failed to initialize the plan node: type - " + type, e);
    }
  }
}
