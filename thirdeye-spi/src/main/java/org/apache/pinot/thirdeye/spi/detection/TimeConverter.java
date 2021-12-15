package org.apache.pinot.thirdeye.spi.detection;

import org.apache.pinot.thirdeye.spi.dataframe.LongSeries;
import org.apache.pinot.thirdeye.spi.dataframe.Series;
import org.apache.pinot.thirdeye.spi.dataframe.Series.ObjectFunction;

public interface TimeConverter {

  /**
   * Convert incoming time value string to milliseconds epoch value.
   *
   * @return milliseconds epoch value
   */
  long convert(String timeValue);

  /**
   * Convert back millis to String
   *
   * @param time
   * @return
   */
  String convertMillis(long time);

  /**
   * Convert incoming time series value to milliseconds epoch long series.
   *
   * @return milliseconds epoch long series.
   */
  default LongSeries convertSeries(final Series series) {
    return series.map((ObjectFunction) values -> convert(String.valueOf(values[0]))).getLongs();
  }
}
