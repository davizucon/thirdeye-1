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

package org.apache.pinot.thirdeye.anomaly.task;

import org.apache.pinot.thirdeye.anomaly.monitor.MonitorTaskRunner;
import org.apache.pinot.thirdeye.anomaly.task.TaskConstants.TaskType;
import org.apache.pinot.thirdeye.detection.DetectionPipelineTaskRunner;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertTaskRunner;
import org.apache.pinot.thirdeye.detection.dataquality.DataQualityPipelineTaskRunner;
import org.apache.pinot.thirdeye.detection.onboard.YamlOnboardingTaskRunner;

/**
 * This class returns an instance of the task runner depending on the task type
 */
public class TaskRunnerFactory {

  public static TaskRunner get(TaskType taskType) {
    switch (taskType) {
      case DATA_QUALITY:
        return new DataQualityPipelineTaskRunner();
      case DETECTION:
        return new DetectionPipelineTaskRunner();
      case DETECTION_ALERT:
        return new DetectionAlertTaskRunner();
      case YAML_DETECTION_ONBOARD:
        return new YamlOnboardingTaskRunner();
      case MONITOR:
        return new MonitorTaskRunner();
      default:
        throw new RuntimeException("Invalid TaskType: " + taskType);
    }
  }
}
