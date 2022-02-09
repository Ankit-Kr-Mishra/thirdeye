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
package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.Grouper;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A sample mock grouper to test the Grouper Interface
 */
public class MockGrouper implements Grouper<MockGrouperSpec> {

  private static final String mockDimKey = "mock_dimension_name";
  private static final String mockDimValue = "test_value";

  @Override
  public void init(MockGrouperSpec spec, InputDataFetcher dataFetcher) {
  }

  @Override
  public void init(final MockGrouperSpec spec) {

  }

  @Override
  public List<MergedAnomalyResultDTO> group(List<MergedAnomalyResultDTO> anomalies) {
    // A sample code for testing the grouper interface.
    List<MergedAnomalyResultDTO> groupedAnomalies = new ArrayList<>();
    for (MergedAnomalyResultDTO anomaly : anomalies) {
      if (anomaly != null && anomaly.getDimensions() != null
          && anomaly.getDimensions().get(mockDimKey) != null) {
        if (anomaly.getDimensions().get(mockDimKey).equals(mockDimValue)) {
          Map<String, String> properties = new HashMap<>();
          properties.put("TEST_KEY", "TEST_VALUE");
          anomaly.setProperties(properties);
          groupedAnomalies.add(anomaly);
        }
      }
    }

    return groupedAnomalies;
  }
}