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

package org.apache.pinot.thirdeye.detection.components.detectors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.pinot.thirdeye.spi.dataframe.util.MetricSlice;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ThresholdRuleDetectorSpec extends AbstractSpec {

  private double min = Double.NaN;
  private double max = Double.NaN;
  private String monitoringGranularity = MetricSlice.NATIVE_GRANULARITY
      .toAggregationGranularityString(); // use native granularity by default

  public String getMonitoringGranularity() {
    return monitoringGranularity;
  }

  public void setMonitoringGranularity(String monitoringGranularity) {
    this.monitoringGranularity = monitoringGranularity;
  }

  public double getMin() {
    return min;
  }

  public void setMin(double min) {
    this.min = min;
  }

  public double getMax() {
    return max;
  }

  public void setMax(double max) {
    this.max = max;
  }
}