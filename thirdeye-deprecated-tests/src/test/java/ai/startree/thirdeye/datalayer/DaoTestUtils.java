/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer;

import static ai.startree.thirdeye.detection.alert.StatefulDetectionAlertFilter.PROP_BCC;
import static ai.startree.thirdeye.detection.alert.StatefulDetectionAlertFilter.PROP_CC;
import static ai.startree.thirdeye.detection.alert.StatefulDetectionAlertFilter.PROP_RECIPIENTS;
import static ai.startree.thirdeye.detection.alert.StatefulDetectionAlertFilter.PROP_TO;

import ai.startree.thirdeye.detection.detector.email.filter.AlphaBetaAlertFilter;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.datalayer.bao.OverrideConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertSnapshotDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DetectionStatusDTO;
import ai.startree.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import ai.startree.thirdeye.spi.datalayer.dto.JobDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.OnboardDatasetMetricDTO;
import ai.startree.thirdeye.spi.datalayer.dto.OverrideConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RootcauseSessionDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFeedConfig;
import ai.startree.thirdeye.spi.detection.AnomalyFetcherConfig;
import ai.startree.thirdeye.spi.detection.AnomalyNotifiedStatus;
import ai.startree.thirdeye.spi.detection.AnomalySource;
import ai.startree.thirdeye.spi.detection.MetricAggFunction;
import ai.startree.thirdeye.spi.detection.metric.MetricType;
import ai.startree.thirdeye.spi.task.TaskType;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTime;

public class DaoTestUtils {

  public static AnomalyFunctionDTO getTestFunctionSpec(String metricName, String collection) {
    AnomalyFunctionDTO functionSpec = new AnomalyFunctionDTO();
    functionSpec.setFunctionName("integration test function 1");
    functionSpec.setType("WEEK_OVER_WEEK_RULE");
    functionSpec.setTopicMetric(metricName);
    functionSpec.setMetrics(Arrays.asList(metricName));
    functionSpec.setCollection(collection);
    functionSpec.setMetricFunction(MetricAggFunction.SUM);
    functionSpec.setCron("0/10 * * * * ?");
    functionSpec.setBucketSize(1);
    functionSpec.setBucketUnit(TimeUnit.HOURS);
    functionSpec.setWindowDelay(3);
    functionSpec.setWindowDelayUnit(TimeUnit.HOURS);
    functionSpec.setWindowSize(1);
    functionSpec.setWindowUnit(TimeUnit.DAYS);
    functionSpec.setProperties("baseline=w/w;changeThreshold=0.001;min=100;max=900");
    functionSpec.setIsActive(true);
    functionSpec.setRequiresCompletenessCheck(false);
    functionSpec.setSecondaryAnomalyFunctionsType(Arrays.asList("MIN_MAX_THRESHOLD"));
    return functionSpec;
  }

  public static AnomalyFunctionDTO getTestFunctionAlphaBetaAlertFilterSpec(String metricName,
      String collection) {
    AnomalyFunctionDTO functionSpec = getTestFunctionSpec(metricName, collection);
    Map<String, String> alphaBetaAlertFilter = new HashMap<>();
    alphaBetaAlertFilter.put("type", "alpha_beta");
    alphaBetaAlertFilter.put(AlphaBetaAlertFilter.ALPHA, "1");
    alphaBetaAlertFilter.put(AlphaBetaAlertFilter.BETA, "1");
    alphaBetaAlertFilter.put(AlphaBetaAlertFilter.THRESHOLD, "0.5");
    functionSpec.setAlertFilter(alphaBetaAlertFilter);
    return functionSpec;
  }

  public static SubscriptionGroupDTO getTestNotificationConfig(String name) {
    SubscriptionGroupDTO notificationConfigDTO = new SubscriptionGroupDTO();
    notificationConfigDTO.setName(name);
    notificationConfigDTO.setActive(true);
    notificationConfigDTO.setApplication("test");
    notificationConfigDTO.setFrom("te@linkedin.com");
    notificationConfigDTO.setCronExpression("0/10 * * * * ?");

    Map<String, Object> properties = new HashMap<>();
    Map<String, Set<String>> recipients = new HashMap<>();
    recipients.put(PROP_TO, Collections.singleton("anomaly-to@linedin.com"));
    recipients.put(PROP_CC, Collections.singleton("anomaly-cc@linedin.com"));
    recipients.put(PROP_BCC, Collections.singleton("anomaly-bcc@linedin.com"));
    properties.put(PROP_RECIPIENTS, recipients);
    notificationConfigDTO.setProperties(properties);

    Map<Long, Long> vectorClocks = new HashMap<>();
    notificationConfigDTO.setVectorClocks(vectorClocks);

    return notificationConfigDTO;
  }

  public static JobDTO getTestJobSpec() {
    JobDTO jobSpec = new JobDTO();
    jobSpec.setJobName("Test_Anomaly_Job");
    jobSpec.setStatus(Constants.JobStatus.SCHEDULED);
    jobSpec.setTaskType(TaskType.DETECTION);
    jobSpec.setScheduleStartTime(System.currentTimeMillis());
    jobSpec.setWindowStartTime(new DateTime().minusHours(20).getMillis());
    jobSpec.setWindowEndTime(new DateTime().minusHours(10).getMillis());
    jobSpec.setConfigId(100);
    return jobSpec;
  }

  public static DatasetConfigDTO getTestDatasetConfig(String collection) {
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setDataset(collection);
    datasetConfigDTO.setDimensions(Lists.newArrayList("country", "browser", "environment"));
    datasetConfigDTO.setTimeColumn("time");
    datasetConfigDTO.setTimeDuration(1);
    datasetConfigDTO.setTimeUnit(TimeUnit.HOURS);
    datasetConfigDTO.setActive(true);
    datasetConfigDTO.setDataSource("PinotThirdEyeDataSource");
    datasetConfigDTO.setLastRefreshTime(System.currentTimeMillis());
    return datasetConfigDTO;
  }

  public static MetricConfigDTO getTestMetricConfig(String collection, String metric, Long id) {
    MetricConfigDTO metricConfigDTO = new MetricConfigDTO();
    if (id != null) {
      metricConfigDTO.setId(id);
    }
    metricConfigDTO.setDataset(collection);
    metricConfigDTO.setDatatype(MetricType.LONG);
    metricConfigDTO.setName(metric);
    metricConfigDTO.setAlias(SpiUtils.constructMetricAlias(collection, metric));
    return metricConfigDTO;
  }

  public static OverrideConfigDTO getTestOverrideConfigForTimeSeries(DateTime now) {
    OverrideConfigDTO overrideConfigDTO = new OverrideConfigDTO();
    overrideConfigDTO.setStartTime(now.minusHours(8).getMillis());
    overrideConfigDTO.setEndTime(now.plusHours(8).getMillis());
    overrideConfigDTO.setTargetEntity(OverrideConfigManager.ENTITY_TIME_SERIES);
    overrideConfigDTO.setActive(true);

    Map<String, String> overrideProperties = new HashMap<>();
    overrideProperties.put(Constants.SCALING_FACTOR, "1.2");
    overrideConfigDTO.setOverrideProperties(overrideProperties);

    Map<String, List<String>> overrideTarget = new HashMap<>();
    overrideTarget
        .put(OverrideConfigManager.TARGET_COLLECTION, Arrays.asList("collection1", "collection2"));
    overrideTarget.put(OverrideConfigManager.EXCLUDED_COLLECTION, Arrays.asList("collection3"));
    overrideConfigDTO.setTargetLevel(overrideTarget);

    return overrideConfigDTO;
  }

  public static DetectionStatusDTO getTestDetectionStatus(String dataset, long dateToCheckInMS,
      String dateToCheckInSDF, boolean detectionRun, long functionId) {
    DetectionStatusDTO detectionStatusDTO = new DetectionStatusDTO();
    detectionStatusDTO.setDataset(dataset);
    detectionStatusDTO.setFunctionId(functionId);
    detectionStatusDTO.setDateToCheckInMS(dateToCheckInMS);
    detectionStatusDTO.setDateToCheckInSDF(dateToCheckInSDF);
    detectionStatusDTO.setDetectionRun(detectionRun);
    return detectionStatusDTO;
  }

  public static EntityToEntityMappingDTO getTestEntityToEntityMapping(String fromURN, String toURN,
      String mappingType) {
    EntityToEntityMappingDTO dto = new EntityToEntityMappingDTO();
    dto.setFromURN(fromURN);
    dto.setToURN(toURN);
    dto.setMappingType(mappingType);
    dto.setScore(1);
    return dto;
  }

  public static OnboardDatasetMetricDTO getTestOnboardConfig(String datasetName, String metricName,
      String dataSource) {
    OnboardDatasetMetricDTO dto = new OnboardDatasetMetricDTO();
    dto.setDatasetName(datasetName);
    dto.setMetricName(metricName);
    dto.setDataSource(dataSource);
    return dto;
  }

  public static AlertSnapshotDTO getTestAlertSnapshot() {
    AlertSnapshotDTO alertSnapshot = new AlertSnapshotDTO();
    alertSnapshot.setLastNotifyTime(0);
    Multimap<String, AnomalyNotifiedStatus> snapshot = HashMultimap.create();
    snapshot.put("test::{dimension=[test]}", new AnomalyNotifiedStatus(0, -0.1));
    snapshot.put("test::{dimension=[test]}", new AnomalyNotifiedStatus(2, -0.2));
    snapshot.put("test::{dimension=[test2]}", new AnomalyNotifiedStatus(4, -0.4));
    alertSnapshot.setSnapshot(snapshot);
    return alertSnapshot;
  }

  public static AnomalyFeedConfig getTestAnomalyFeedConfig() {
    AnomalyFeedConfig anomalyFeedConfig = new AnomalyFeedConfig();
    anomalyFeedConfig.setAnomalyFeedType("UnionAnomalyFeed");
    anomalyFeedConfig.setAnomalySourceType(AnomalySource.METRIC);
    anomalyFeedConfig.setAnomalySource("test");
    anomalyFeedConfig.setAlertSnapshotId(1l);

    AnomalyFetcherConfig anomalyFetcherConfig = getTestAnomalyFetcherConfig();
    List<AnomalyFetcherConfig> fetcherConfigs = new ArrayList<>();
    fetcherConfigs.add(anomalyFetcherConfig);
    anomalyFeedConfig.setAnomalyFetcherConfigs(fetcherConfigs);

    Map<String, String> alertFilterConfig = new HashMap<>();
    alertFilterConfig.put(AlphaBetaAlertFilter.TYPE, "DUMMY");
    alertFilterConfig.put("properties", "");
    List<Map<String, String>> filterConfigs = new ArrayList<>();
    filterConfigs.add(alertFilterConfig);
    anomalyFeedConfig.setAlertFilterConfigs(filterConfigs);

    return anomalyFeedConfig;
  }

  public static AnomalyFetcherConfig getTestAnomalyFetcherConfig() {
    AnomalyFetcherConfig anomalyFetcherConfig = new AnomalyFetcherConfig();
    anomalyFetcherConfig.setType("UnnotifiedAnomalyFetcher");
    anomalyFetcherConfig.setProperties("");
    anomalyFetcherConfig.setAnomalySourceType(AnomalySource.METRIC);
    anomalyFetcherConfig.setAnomalySource("test");
    return anomalyFetcherConfig;
  }

  public static MergedAnomalyResultDTO getTestMergedAnomalyResult(long startTime, long endTime,
      String collection,
      String metric, double weight, long functionId, long createdTime) {
    // Add mock anomalies
    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setStartTime(startTime);
    anomaly.setEndTime(endTime);
    anomaly.setCollection(collection);
    anomaly.setMetric(metric);
    anomaly.setWeight(weight);
    anomaly.setFunctionId(functionId);
    anomaly.setDetectionConfigId(functionId);
    anomaly.setCreatedTime(createdTime);

    return anomaly;
  }

  public static MergedAnomalyResultDTO getTestGroupedAnomalyResult(long startTime, long endTime,
      long createdTime, long id) {
    MergedAnomalyResultDTO anomaly = new MergedAnomalyResultDTO();
    anomaly.setStartTime(startTime);
    anomaly.setEndTime(endTime);
    anomaly.setCollection(null);
    anomaly.setMetric(null);
    anomaly.setCreatedTime(createdTime);
    anomaly.setDetectionConfigId(id);

    return anomaly;
  }

  public static RootcauseSessionDTO getTestRootcauseSessionResult(long start, long end,
      long created, long updated,
      String name, String owner, String text, String granularity, String compareMode,
      Long previousId, Long anomalyId) {
    RootcauseSessionDTO session = new RootcauseSessionDTO();
    session.setAnomalyRangeStart(start);
    session.setAnomalyRangeEnd(end);
    session.setAnalysisRangeStart(start - 100);
    session.setAnalysisRangeEnd(end + 100);
    session.setName(name);
    session.setOwner(owner);
    session.setText(text);
    session.setPreviousId(previousId);
    session.setAnomalyId(anomalyId);
    session.setCreated(created);
    session.setUpdated(updated);
    session.setGranularity(granularity);
    session.setCompareMode(compareMode);
    return session;
  }
}
