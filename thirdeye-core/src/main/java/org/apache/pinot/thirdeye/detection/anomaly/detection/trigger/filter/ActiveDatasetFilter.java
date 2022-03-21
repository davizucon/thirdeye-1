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

package org.apache.pinot.thirdeye.detection.anomaly.detection.trigger.filter;

import org.apache.pinot.thirdeye.detection.anomaly.detection.trigger.DataAvailabilityEvent;
import org.apache.pinot.thirdeye.detection.anomaly.detection.trigger.utils.DatasetTriggerInfoRepo;

/**
 * This class is to filter out events that are not in the list of active dataset.
 */
public class ActiveDatasetFilter implements DataAvailabilityEventFilter {

  private final DatasetTriggerInfoRepo datasetTriggerInfoRepo;

  public ActiveDatasetFilter(final DatasetTriggerInfoRepo datasetTriggerInfoRepo) {
    this.datasetTriggerInfoRepo = datasetTriggerInfoRepo;
  }

  @Override
  public boolean isPassed(DataAvailabilityEvent e) {
    return datasetTriggerInfoRepo.isDatasetActive(e.getDatasetName());
  }
}
