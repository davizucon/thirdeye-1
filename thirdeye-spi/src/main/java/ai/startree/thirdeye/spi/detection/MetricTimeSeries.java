/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection;

import java.util.Set;
import org.joda.time.Interval;

public interface MetricTimeSeries {

  /**
   * Get data value for a given timestamp
   *
   * @return the corresponding value
   */
  Double get(long timestamp);

  /**
   * Remove anomalies data if needed
   */
  void remove(long timeStamp);

  /**
   * Contain timestamp or not
   *
   * @return true or false
   */
  boolean hasTimestamp(long timestamp);

  /**
   * Get timestamp set
   *
   * @return set
   */
  Set<Long> timestampSet();

  /**
   * Returns the interval of the time series, which provides the max and min timestamps (inclusive).
   */
  Interval getTimeSeriesInterval();

  /**
   * Get the size of the timestamp set
   *
   * @return the size of the number of timestamps in the series
   */
  int size();
}
