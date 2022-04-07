/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.model;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.util.MetricSlice;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Input data for each detection stage
 */
public class InputData {

  final InputDataSpec dataSpec;
  final Map<MetricSlice, DataFrame> timeseries;
  final Map<MetricSlice, DataFrame> aggregates;
  final Multimap<AnomalySlice, MergedAnomalyResultDTO> anomalies;
  final Multimap<EventSlice, EventDTO> events;
  final Map<Long, MetricConfigDTO> metrics;
  final Map<String, DatasetConfigDTO> datasets;
  final Multimap<EvaluationSlice, EvaluationDTO> evaluations;

  /**
   * The data set config dtos for metric ids
   *
   * @see InputDataSpec#withMetricIdsForDataset(Collection)
   */
  final Map<Long, DatasetConfigDTO> datasetForMetricId;

  /**
   * The metric config dtos for metric and data set names
   *
   * @see InputDataSpec#withMetricNamesAndDatasetNames(Collection)
   */
  final Map<InputDataSpec.MetricAndDatasetName, MetricConfigDTO> metricForMetricAndDatasetNames;

  public InputData(InputDataSpec spec, Map<MetricSlice, DataFrame> timeseries,
      Map<MetricSlice, DataFrame> aggregates,
      Multimap<AnomalySlice, MergedAnomalyResultDTO> anomalies,
      Multimap<EventSlice, EventDTO> events) {
    this.dataSpec = spec;
    this.timeseries = timeseries;
    this.aggregates = aggregates;
    this.anomalies = anomalies;
    this.events = events;
    this.metrics = Collections.emptyMap();
    this.datasets = Collections.emptyMap();
    this.datasetForMetricId = Collections.emptyMap();
    this.metricForMetricAndDatasetNames = Collections.emptyMap();
    this.evaluations = ArrayListMultimap.create();
  }

  public InputData(InputDataSpec spec, Map<MetricSlice, DataFrame> timeseries,
      Map<MetricSlice, DataFrame> aggregates,
      Multimap<AnomalySlice, MergedAnomalyResultDTO> anomalies,
      Multimap<EventSlice, EventDTO> events,
      Map<Long, MetricConfigDTO> metrics, Map<String, DatasetConfigDTO> datasets,
      Multimap<EvaluationSlice, EvaluationDTO> evaluations,
      Map<Long, DatasetConfigDTO> datasetForMetricId,
      Map<InputDataSpec.MetricAndDatasetName, MetricConfigDTO> metricForMetricAndDatasetNames) {
    this.dataSpec = spec;
    this.timeseries = timeseries;
    this.aggregates = aggregates;
    this.anomalies = anomalies;
    this.events = events;
    this.metrics = metrics;
    this.datasets = datasets;
    this.evaluations = evaluations;
    this.datasetForMetricId = datasetForMetricId;
    this.metricForMetricAndDatasetNames = metricForMetricAndDatasetNames;
  }

  public InputDataSpec getDataSpec() {
    return dataSpec;
  }

  public Map<MetricSlice, DataFrame> getTimeseries() {
    return timeseries;
  }

  public Map<MetricSlice, DataFrame> getAggregates() {
    return aggregates;
  }

  public Multimap<AnomalySlice, MergedAnomalyResultDTO> getAnomalies() {
    return anomalies;
  }

  public Multimap<EventSlice, EventDTO> getEvents() {
    return events;
  }

  public Map<Long, MetricConfigDTO> getMetrics() {
    return metrics;
  }

  public Map<String, DatasetConfigDTO> getDatasets() {
    return datasets;
  }

  public Map<Long, DatasetConfigDTO> getDatasetForMetricId() {
    return datasetForMetricId;
  }

  public Map<InputDataSpec.MetricAndDatasetName, MetricConfigDTO> getMetricForMetricAndDatasetNames() {
    return metricForMetricAndDatasetNames;
  }

  public Multimap<EvaluationSlice, EvaluationDTO> getEvaluations() {
    return evaluations;
  }
}
