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

package org.apache.pinot.thirdeye.notification.content.templates;

import static java.util.Comparator.comparingDouble;

import com.google.common.base.Joiner;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.datasource.cache.DataSourceCache;
import org.apache.pinot.thirdeye.detection.anomaly.detection.AnomalyDetectionInputContextBuilder;
import org.apache.pinot.thirdeye.metric.MetricTimeSeries;
import org.apache.pinot.thirdeye.notification.content.AnomalyReportEntity;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent;
import org.apache.pinot.thirdeye.spi.Constants.CompareMode;
import org.apache.pinot.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.EventManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.spi.datalayer.bao.MetricConfigManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.EventDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import org.apache.pinot.thirdeye.spi.detection.AnomalyFeedback;
import org.apache.pinot.thirdeye.spi.detection.AnomalyResult;
import org.apache.pinot.thirdeye.spi.util.Pair;
import org.apache.pinot.thirdeye.spi.util.SpiUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Period;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This email content formatter provides a hierarchical view of anomalies. It categorizes the
 * anomalies by its dimensions.
 * The top-level anomalies are defined as anomalies generated by anomaly function without dimension
 * drill-down; otherwise,
 * it is in the lower-level anomalies. The content formatter takes hierarchical-anomalies-email-template.ftl
 * in default.
 */
public class HierarchicalAnomaliesContent extends BaseNotificationContent {

  private static final Logger LOG = LoggerFactory.getLogger(HierarchicalAnomaliesContent.class);

  private static final String PRESENT_SEASONAL_VALUES = "presentSeasonalValues";
  private static final String DEFAULT_PRESENT_SEASONAL_VALUES = "false";
  private final ThirdEyeCacheRegistry thirdEyeCacheRegistry;
  private final DataSourceCache dataSourceCache;
  private final DatasetConfigManager datasetConfigManager;
  private boolean presentSeasonalValues;
  private Set<EventDTO> relatedEvents;

  public HierarchicalAnomaliesContent(final DataSourceCache dataSourceCache,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry,
      final MetricConfigManager metricConfigManager,
      final DatasetConfigManager datasetConfigManager, final EventManager eventManager,
      final MergedAnomalyResultManager mergedAnomalyResultManager) {
    super(metricConfigManager, eventManager, mergedAnomalyResultManager);
    this.dataSourceCache = dataSourceCache;
    this.thirdEyeCacheRegistry = thirdEyeCacheRegistry;
    this.datasetConfigManager = datasetConfigManager;
  }

  @Override
  public void init(Properties properties, ThirdEyeCoordinatorConfiguration config) {
    super.init(properties, config);
    relatedEvents = new HashSet<>();
    presentSeasonalValues = Boolean.parseBoolean(
        properties.getProperty(PRESENT_SEASONAL_VALUES, DEFAULT_PRESENT_SEASONAL_VALUES));
  }

  @Override
  public String getTemplate() {
    return HierarchicalAnomaliesContent.class.getSimpleName();
  }

  @Override
  public Map<String, Object> format(Collection<AnomalyResult> anomalies,
      SubscriptionGroupDTO subsConfig) {
    Map<String, Object> templateData = super.getTemplateData(subsConfig, anomalies);
    enrichMetricInfo(templateData, anomalies);
    List<AnomalyReportEntity> rootAnomalyDetails = new ArrayList<>();
    SortedMap<String, List<AnomalyReportEntity>> leafAnomalyDetails = new TreeMap<>();
    List<String> anomalyIds = new ArrayList<>();
    List<AnomalyResult> anomalyList = new ArrayList<>(anomalies);
    anomalyList.sort(comparingDouble(AnomalyResult::getWeight));

    for (AnomalyResult anomalyResult : anomalyList) {
      if (!(anomalyResult instanceof MergedAnomalyResultDTO)) {
        LOG.warn("Anomaly result {} isn't an instance of MergedAnomalyResultDTO. Skip from alert.",
            anomalyResult);
        continue;
      }
      MergedAnomalyResultDTO anomaly = (MergedAnomalyResultDTO) anomalyResult;

      // include notified alerts only in the email
      if (includeSentAnomaliesOnly) {
        if (anomaly.isNotified()) {
          putAnomaliesIntoRootOrLeaf(anomaly, rootAnomalyDetails, leafAnomalyDetails);
          anomalyIds.add(Long.toString(anomaly.getId()));
        }
      } else {
        putAnomaliesIntoRootOrLeaf(anomaly, rootAnomalyDetails, leafAnomalyDetails);
        anomalyIds.add(Long.toString(anomaly.getId()));
      }
    }
    List<EventDTO> sortedEvents = new ArrayList<>(relatedEvents);
    Collections.sort(sortedEvents, new Comparator<EventDTO>() {
      @Override
      public int compare(EventDTO o1, EventDTO o2) {
        return Long.compare(o1.getStartTime(), o2.getStartTime());
      }
    });
    templateData.put("containsSeasonal", presentSeasonalValues);
    templateData.put("rootAnomalyDetails", rootAnomalyDetails);
    templateData.put("leafAnomalyDetails", leafAnomalyDetails);
    templateData.put("holidays", sortedEvents);
    templateData.put("anomalyIds", Joiner.on(",").join(anomalyIds));

    return templateData;
  }

  /**
   * Generate the AnomalyReportEntity
   */
  private AnomalyReportEntity generateAnomalyReportEntity(MergedAnomalyResultDTO anomaly,
      String dashboardHost) {
    AnomalyFeedback feedback = anomaly.getFeedback();

    String feedbackVal = getFeedbackValue(feedback);

    Properties props = new Properties();
    props.putAll(anomaly.getProperties());
    double lift = BaseNotificationContent
        .getLift(anomaly.getAvgCurrentVal(), anomaly.getAvgBaselineVal());
    AnomalyReportEntity
        anomalyReport = new AnomalyReportEntity(String.valueOf(anomaly.getId()),
        getAnomalyURL(anomaly, dashboardHost),
        getPredictedValue(anomaly),
        getCurrentValue(anomaly),
        getFormattedLiftValue(anomaly, lift),
        getLiftDirection(lift),
        anomaly.getImpactToGlobal(),
        getDimensionsList(anomaly.getDimensionMap()),
        getTimeDiffInHours(anomaly.getStartTime(), anomaly.getEndTime()), // duration
        feedbackVal,
        anomaly.getAnomalyFunction().getFunctionName(),
        "",
        anomaly.getMetric(),
        getDateString(anomaly.getStartTime(), dateTimeZone),
        getDateString(anomaly.getEndTime(), dateTimeZone),
        getTimezoneString(dateTimeZone),
        getIssueType(anomaly),
        anomaly.getType().getLabel(),
        SpiUtils.encodeCompactedProperties(props),
        anomaly.getMetricUrn()
    );

    List<String> affectedCountries = getMatchedFilterValues(anomaly, "country");
    if (affectedCountries.size() > 0) { // if the anomaly is on country level
      Map<String, List<String>> targetDimensions = new HashMap<>();
      targetDimensions.put(EVENT_FILTER_COUNTRY, affectedCountries);
      relatedEvents.addAll(getHolidayEvents(
          new DateTime(anomaly.getStartTime(), dateTimeZone),
          new DateTime(anomaly.getEndTime(), dateTimeZone),
          targetDimensions));
    }

    return anomalyReport;
  }

  /**
   * Generate the AnomalyReportEntity and determine if the given anomaly is root or leaf level
   */
  private AnomalyReportEntity putAnomaliesIntoRootOrLeaf(MergedAnomalyResultDTO anomaly,
      List<AnomalyReportEntity> rootAnomalyDetail,
      SortedMap<String, List<AnomalyReportEntity>> leafAnomalyDetail) {
    AnomalyReportEntity anomalyReport = generateAnomalyReportEntity(anomaly,
        thirdEyeAnomalyConfig.getUiConfiguration().getExternalUrl());
    AnomalyFunctionDTO anomalyFunction = anomaly.getAnomalyFunction();
    String exploredDimensions = anomalyFunction.getExploreDimensions();
    // Add WoW number
    if (presentSeasonalValues) {
      for (CompareMode compareMode : CompareMode.values()) {
        double avgValues = Double.NaN;
        try {
          avgValues = getAvgComparisonBaseline(anomaly, compareMode, anomaly.getStartTime(),
              anomaly.getEndTime());
        } catch (Exception e) {
          LOG.warn("Unable to fetch wow information for {}.", anomalyFunction);
        }
        anomalyReport.setSeasonalValues(compareMode, avgValues, anomaly.getAvgCurrentVal());
      }
    }
    if (StringUtils.isBlank(exploredDimensions)) {
      rootAnomalyDetail.add(anomalyReport);
    } else {
      if (!leafAnomalyDetail.containsKey(exploredDimensions)) {
        leafAnomalyDetail.put(exploredDimensions, new ArrayList<AnomalyReportEntity>());
      }
      leafAnomalyDetail.get(exploredDimensions).add(anomalyReport);
    }
    return anomalyReport;
  }

  /**
   * Retrieve the average wow baseline values
   *
   * @param anomaly an instance of MergedAnomalyResultDTO
   * @param compareMode the way to compare, WoW, Wo2W, Wo3W, and Wo4W
   * @param start the start time of the monitoring window in millis
   * @param end the end time of the monitoring window in millis
   * @return baseline values based on compareMode
   */
  private Double getAvgComparisonBaseline(MergedAnomalyResultDTO anomaly, CompareMode compareMode,
      long start, long end) throws Exception {
    AnomalyFunctionDTO anomalyFunction = anomaly.getAnomalyFunction();
    DatasetConfigDTO datasetConfigDTO = datasetConfigManager
        .findByDataset(anomalyFunction.getCollection());
    AnomalyDetectionInputContextBuilder contextBuilder = new AnomalyDetectionInputContextBuilder(
        dataSourceCache,
        thirdEyeCacheRegistry,
        datasetConfigManager);
    contextBuilder.setFunction(anomalyFunction);

    DateTimeZone timeZone = DateTimeZone.forID(datasetConfigDTO.getTimezone());
    DateTime startTime = new DateTime(start, timeZone);
    DateTime endTime = new DateTime(end, timeZone);

    Period baselinePeriod = getBaselinePeriod(compareMode);
    DateTime baselineStartTime = startTime.minus(baselinePeriod);
    DateTime baselineEndTime = endTime.minus(baselinePeriod);

    Pair<Long, Long> timeRange = new Pair<>(baselineStartTime.getMillis(),
        baselineEndTime.getMillis());
    MetricTimeSeries
        baselineTimeSeries = contextBuilder
        .fetchTimeSeriesDataByDimension(Arrays.asList(timeRange), anomaly.getDimensions(), false,
            datasetConfigDTO)
        .build().getDimensionMapMetricTimeSeriesMap().get(anomaly.getDimensions());

    return baselineTimeSeries.getMetricAvgs(0d)[0];
  }
}
