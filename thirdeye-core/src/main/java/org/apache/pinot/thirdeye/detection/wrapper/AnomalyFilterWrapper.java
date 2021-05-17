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

package org.apache.pinot.thirdeye.detection.wrapper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Collections2;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.MapUtils;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.EvaluationDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.detection.ConfigUtils;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.DetectionPipeline;
import org.apache.pinot.thirdeye.detection.DetectionPipelineResultV1;
import org.apache.pinot.thirdeye.detection.DetectionUtils;
import org.apache.pinot.thirdeye.detection.PredictionResult;
import org.apache.pinot.thirdeye.detection.spi.components.AnomalyFilter;

/**
 * This anomaly filter wrapper runs the anomaly filter component to filter anomalies generated by
 * detector based on the filter implementation.
 */
public class AnomalyFilterWrapper extends DetectionPipeline {

  private static final String PROP_NESTED = "nested";
  private static final String PROP_METRIC_URN = "metricUrn";
  private static final String PROP_FILTER = "filter";

  private final List<Map<String, Object>> nestedProperties;
  private final AnomalyFilter anomalyFilter;
  private final String metricUrn;

  public AnomalyFilterWrapper(DataProvider provider, AlertDTO config, long startTime,
      long endTime) {
    super(provider, config, startTime, endTime);
    Map<String, Object> properties = config.getProperties();
    this.nestedProperties = ConfigUtils.getList(properties.get(PROP_NESTED));

    Preconditions.checkArgument(this.config.getProperties().containsKey(PROP_FILTER));
    String detectorReferenceKey = DetectionUtils
        .getComponentKey(MapUtils.getString(config.getProperties(), PROP_FILTER));
    Preconditions.checkArgument(this.config.getComponents().containsKey(detectorReferenceKey));
    this.anomalyFilter = (AnomalyFilter) this.config.getComponents().get(detectorReferenceKey);

    this.metricUrn = MapUtils.getString(properties, PROP_METRIC_URN);
  }

  /**
   * Runs the nested pipelines and calls the isQualified method in the anomaly filter stage to check
   * if an anomaly passes the filter.
   *
   * @return the detection pipeline result
   */
  @Override
  public final DetectionPipelineResultV1 run() throws Exception {
    List<MergedAnomalyResultDTO> candidates = new ArrayList<>();
    List<PredictionResult> predictionResults = new ArrayList<>();
    Map<String, Object> diagnostics = new HashMap<>();
    List<EvaluationDTO> evaluations = new ArrayList<>();

    Set<Long> lastTimeStamps = new HashSet<>();
    for (Map<String, Object> properties : this.nestedProperties) {
      if (this.metricUrn != null) {
        properties.put(PROP_METRIC_URN, this.metricUrn);
      }
      DetectionPipelineResultV1 intermediate = this
          .runNested(properties, this.startTime, this.endTime);
      lastTimeStamps.add(intermediate.getLastTimestamp());
      diagnostics.putAll(intermediate.getDiagnostics());
      evaluations.addAll(intermediate.getEvaluations());
      predictionResults.addAll(intermediate.getPredictions());
      candidates.addAll(intermediate.getAnomalies());
    }

    Collection<MergedAnomalyResultDTO> anomalies =
        Collections2.filter(candidates,
            mergedAnomaly -> mergedAnomaly != null && !mergedAnomaly.isChild() && anomalyFilter
                .isQualified(mergedAnomaly));

    return new DetectionPipelineResultV1(new ArrayList<>(anomalies),
        DetectionUtils.consolidateNestedLastTimeStamps(lastTimeStamps),
        predictionResults, evaluations).setDiagnostics(diagnostics);
  }
}
