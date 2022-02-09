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

package ai.startree.thirdeye.spi.api.cube;

import java.util.List;

/**
 * A POJO for front-end representation.
 */
public class SummaryResponseRow extends BaseResponseRow {

  private List<String> names;
  private List<String> otherDimensionValues;
  private int moreOtherDimensionNumber;
  private double cost;

  public List<String> getNames() {
    return names;
  }

  public SummaryResponseRow setNames(final List<String> names) {
    this.names = names;
    return this;
  }

  public List<String> getOtherDimensionValues() {
    return otherDimensionValues;
  }

  public SummaryResponseRow setOtherDimensionValues(final List<String> otherDimensionValues) {
    this.otherDimensionValues = otherDimensionValues;
    return this;
  }

  public int getMoreOtherDimensionNumber() {
    return moreOtherDimensionNumber;
  }

  public SummaryResponseRow setMoreOtherDimensionNumber(final int moreOtherDimensionNumber) {
    this.moreOtherDimensionNumber = moreOtherDimensionNumber;
    return this;
  }

  public double getCost() {
    return cost;
  }

  public SummaryResponseRow setCost(final double cost) {
    this.cost = cost;
    return this;
  }
}