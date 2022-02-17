/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.util;

import ai.startree.thirdeye.datasource.MetricExpression;
import ai.startree.thirdeye.datasource.ThirdEyeCacheRegistry;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datasource.MetricFunction;
import ai.startree.thirdeye.spi.detection.MetricAggFunction;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import ai.startree.thirdeye.spi.util.SpiUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utils {

  private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public static List<MetricExpression> convertToMetricExpressions(String metricsJson,
      MetricAggFunction aggFunction, String dataset,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) throws ExecutionException {

    List<MetricExpression> metricExpressions = new ArrayList<>();
    if (metricsJson == null) {
      return metricExpressions;
    }
    ArrayList<String> metricExpressionNames;
    try {
      TypeReference<ArrayList<String>> valueTypeRef = new TypeReference<ArrayList<String>>() {
      };
      metricExpressionNames = OBJECT_MAPPER.readValue(metricsJson, valueTypeRef);
    } catch (Exception e) {
      metricExpressionNames = new ArrayList<>();
      String[] metrics = metricsJson.split(",");
      for (String metric : metrics) {
        metricExpressionNames.add(metric.trim());
      }
    }
    for (String metricExpressionName : metricExpressionNames) {
      String derivedMetricExpression = ThirdEyeUtils
          .getDerivedMetricExpression(metricExpressionName, dataset,
              thirdEyeCacheRegistry);
      MetricExpression metricExpression = new MetricExpression(metricExpressionName,
          derivedMetricExpression,
          aggFunction, dataset);
      metricExpressions.add(metricExpression);
    }
    return metricExpressions;
  }

  public static List<MetricFunction> computeMetricFunctionsFromExpressions(
      List<MetricExpression> metricExpressions, final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    Set<MetricFunction> metricFunctions = new HashSet<>();

    for (MetricExpression expression : metricExpressions) {
      metricFunctions.addAll(expression.computeMetricFunctions(
          thirdEyeCacheRegistry));
    }
    return Lists.newArrayList(metricFunctions);
  }

  /**
   * Given a duration (in millis), a time granularity, and the target number of chunk to divide the
   * duration, this method returns the time granularity that is able to divide the duration to a
   * number of chunks that is fewer than or equals to the target number.
   *
   * For example, if the duration is 25 hours, time granularity is HOURS, and target number is 12,
   * then the resized time granularity is 3_HOURS, which divide the duration to 9 chunks.
   *
   * @param duration the duration in milliseconds.
   * @param timeGranularityString time granularity in String format.
   * @param targetChunkNum the target number of chunks.
   * @return the resized time granularity in order to divide the duration to the number of chunks
   *     that is smaller than or equals to the target chunk number.
   */
  public static String resizeTimeGranularity(long duration, String timeGranularityString,
      int targetChunkNum) {
    TimeGranularity timeGranularity = TimeGranularity.fromString(timeGranularityString);

    long timeGranularityMillis = timeGranularity.toMillis();
    long chunkNum = duration / timeGranularityMillis;
    if (duration % timeGranularityMillis != 0) {
      ++chunkNum;
    }
    if (chunkNum > targetChunkNum) {
      long targetIntervalDuration = (long) Math.ceil((double) duration / (double) targetChunkNum);
      long unitTimeGranularityMillis = timeGranularity.getUnit().toMillis(1);
      int size = (int) Math
          .ceil((double) targetIntervalDuration / (double) unitTimeGranularityMillis);
      return size + "_" + timeGranularity.getUnit();
    } else {
      return timeGranularityString;
    }
  }

  /*
   * This method returns the time zone of the data in this collection
   */
  public static DateTimeZone getDateTimeZone(String collection,
      final ThirdEyeCacheRegistry thirdEyeCacheRegistry) {
    DatasetConfigDTO datasetConfig = null;
    try {
      datasetConfig = thirdEyeCacheRegistry.getDatasetConfigCache().get(collection); } catch (ExecutionException e) {
      LOG.error("Exception while getting dataset config for {}", collection);
    }
    return SpiUtils.getDateTimeZone(datasetConfig);
  }

  public static <T> List<T> sublist(List<T> input, int startIndex, int length) {
    startIndex = Math.min(startIndex, input.size());
    final int endIndex = Math.min(startIndex + length, input.size());
    return Lists.newArrayList(input).subList(startIndex, endIndex);
  }
}
