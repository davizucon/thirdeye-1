/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components.filters;

import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.AnomalyFilter;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import ai.startree.thirdeye.spi.detection.model.InputDataSpec;
import ai.startree.thirdeye.spi.rootcause.impl.MetricEntity;
import ai.startree.thirdeye.spi.util.SpiUtils;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/**
 * Aggregate Threshold Filter
 *
 * This threshold rule filter stage filters the anomalies if either the min or max thresholds do not
 * pass. filters the anomalies if either the min or max thresholds do not satisfied.
 */
public class ThresholdRuleAnomalyFilter implements AnomalyFilter<ThresholdRuleFilterSpec> {

  private double minValueHourly;
  private double maxValueHourly;
  private double minValueDaily;
  private double maxValueDaily;
  private double maxValue;
  private double minValue;
  private InputDataFetcher dataFetcher;

  @Override
  public void init(ThresholdRuleFilterSpec spec) {
    this.minValueHourly = spec.getMinValueHourly();
    this.maxValueHourly = spec.getMaxValueHourly();
    this.minValueDaily = spec.getMinValueDaily();
    this.maxValueDaily = spec.getMaxValueDaily();
    this.maxValue = spec.getMaxValue();
    this.minValue = spec.getMinValue();
  }

  @Override
  public void init(ThresholdRuleFilterSpec spec, InputDataFetcher dataFetcher) {
    init(spec);
    this.dataFetcher = dataFetcher;
  }

  @Override
  public boolean isQualified(MergedAnomalyResultDTO anomaly) {
    MetricEntity me = MetricEntity.fromURN(anomaly.getMetricUrn());
    MetricConfigDTO metric = dataFetcher
        .fetchData(new InputDataSpec().withMetricIds(Collections.singleton(me.getId())))
        .getMetrics()
        .get(me.getId());
    double currentValue = anomaly.getAvgCurrentVal();

    Interval anomalyInterval = new Interval(anomaly.getStartTime(), anomaly.getEndTime(), DateTimeZone.UTC);

    // apply multiplier if the metric is aggregated by SUM or COUNT
    double hourlyMultiplier = SpiUtils.isAggCumulative(metric) ?
        (TimeUnit.HOURS.toMillis(1) / (double) anomalyInterval.toDurationMillis()) : 1.0;
    double dailyMultiplier = SpiUtils.isAggCumulative(metric) ?
        (TimeUnit.DAYS.toMillis(1) / (double) anomalyInterval.toDurationMillis()) : 1.0;

    if (!Double.isNaN(this.minValue) && currentValue < this.minValue
        || !Double.isNaN(this.maxValue) && currentValue > this.maxValue) {
      return false;
    }
    if (!Double.isNaN(this.minValueHourly)
        && currentValue * hourlyMultiplier < this.minValueHourly) {
      return false;
    }
    if (!Double.isNaN(this.maxValueHourly)
        && currentValue * hourlyMultiplier > this.maxValueHourly) {
      return false;
    }
    if (!Double.isNaN(this.minValueDaily) && currentValue * dailyMultiplier < this.minValueDaily) {
      return false;
    }
    return Double.isNaN(this.maxValueDaily) || !(currentValue * dailyMultiplier
        > this.maxValueDaily);
  }
}
