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

package org.apache.pinot.thirdeye.task.runner;

import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.pinot.thirdeye.detection.ModelMaintenanceFlow;
import org.apache.pinot.thirdeye.detection.ModelRetuneFlow;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AnomalySubscriptionGroupNotificationManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EvaluationManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EvaluationDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.detection.DetectionUtils;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.task.TaskInfo;
import org.apache.pinot.thirdeye.task.DetectionPipelineTaskInfo;
import org.apache.pinot.thirdeye.task.TaskContext;
import org.apache.pinot.thirdeye.task.TaskResult;
import org.apache.pinot.thirdeye.task.TaskRunner;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DetectionPipelineTaskRunner implements TaskRunner {

  private final Logger LOG = LoggerFactory.getLogger(DetectionPipelineTaskRunner.class);

  private final Counter detectionTaskExceptionCounter;
  private final Counter detectionTaskSuccessCounter;
  private final Counter detectionTaskCounter;

  private final AlertManager alertManager;
  private final EvaluationManager evaluationManager;
  private final ModelMaintenanceFlow modelMaintenanceFlow;
  private final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager;
  private final DetectionPipelineRunner detectionPipelineRunner;
  private final AnomalyMerger anomalyMerger;

  @Inject
  public DetectionPipelineTaskRunner(final AlertManager alertManager,
      final EvaluationManager evaluationManager,
      final ModelRetuneFlow modelMaintenanceFlow,
      final AnomalySubscriptionGroupNotificationManager anomalySubscriptionGroupNotificationManager,
      final MetricRegistry metricRegistry,
      final DetectionPipelineRunner detectionPipelineRunner,
      final AnomalyMerger anomalyMerger) {
    this.alertManager = alertManager;
    this.evaluationManager = evaluationManager;
    this.modelMaintenanceFlow = modelMaintenanceFlow;
    this.anomalySubscriptionGroupNotificationManager = anomalySubscriptionGroupNotificationManager;
    this.detectionPipelineRunner = detectionPipelineRunner;

    detectionTaskExceptionCounter = metricRegistry.counter("detectionTaskExceptionCounter");
    detectionTaskSuccessCounter = metricRegistry.counter("detectionTaskSuccessCounter");
    detectionTaskCounter = metricRegistry.counter("detectionTaskCounter");
    this.anomalyMerger = anomalyMerger;
  }

  @Override
  public List<TaskResult> execute(final TaskInfo taskInfo, final TaskContext taskContext)
      throws Exception {
    detectionTaskCounter.inc();

    try {
      final DetectionPipelineTaskInfo info = (DetectionPipelineTaskInfo) taskInfo;
      final AlertDTO alert = requireNonNull(alertManager.findById(info.getConfigId()),
          String.format("Could not resolve config id %d", info.getConfigId()));

      LOG.info("Start detection for config {} between {} and {}",
          alert.getId(),
          info.getStart(),
          info.getEnd());

      final DetectionPipelineResult result = detectionPipelineRunner.run(
          alert,
          info.getStart(),
          info.getEnd());

      if (result.getLastTimestamp() < 0) {
        LOG.info("No detection ran for config {} between {} and {}",
            alert.getId(),
            info.getStart(),
            info.getEnd());
        return Collections.emptyList();
      }

      postExecution(info, alert, result);

      detectionTaskSuccessCounter.inc();
      LOG.info("End detection for alert {} between {} and {}. Detected {} anomalies.",
          alert.getId(),
          new Date(info.getStart()),
          new Date(info.getEnd()),
          optional(result.getAnomalies()).map(List::size).orElse(0));

      return Collections.emptyList();
    } catch (final Exception e) {
      detectionTaskExceptionCounter.inc();
      throw e;
    }
  }

  private void postExecution(final DetectionPipelineTaskInfo taskInfo,
      final AlertDTO alert, final DetectionPipelineResult result) {
    alert.setLastTimestamp(result.getLastTimestamp());

    anomalyMerger.mergeAndSave(taskInfo, alert, result.getAnomalies());

    for (final EvaluationDTO evaluationDTO : result.getEvaluations()) {
      evaluationManager.save(evaluationDTO);
    }

    try {
      // run maintenance flow to update model
      final AlertDTO updatedConfig = modelMaintenanceFlow.maintain(alert, Instant.now());
      alertManager.update(updatedConfig);
    } catch (final Exception e) {
      LOG.warn("Re-tune pipeline {} failed", alert.getId(), e);
    }

    // re-notify the anomalies if any
    for (final MergedAnomalyResultDTO anomaly : result.getAnomalies()) {
      // if an anomaly should be re-notified, update the notification lookup table in the database
      if (anomaly.isRenotify()) {
        DetectionUtils.renotifyAnomaly(anomaly,
            anomalySubscriptionGroupNotificationManager);
      }
    }
  }
}
