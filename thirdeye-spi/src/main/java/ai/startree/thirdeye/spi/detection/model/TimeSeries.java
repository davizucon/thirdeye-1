/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.model;

import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_LOWER_BOUND;
import static ai.startree.thirdeye.spi.dataframe.DataFrame.COL_UPPER_BOUND;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.dataframe.LongSeries;
import com.google.common.base.Preconditions;

/**
 * Time series. wrapper object of data frame. Used by baselineProvider to return the predicted time
 * series
 */
public class TimeSeries {

  private final DataFrame df;

  private TimeSeries() {
    this.df = new DataFrame();
  }

  public TimeSeries(LongSeries timestamps, DoubleSeries baselineValues) {
    this.df = new DataFrame();
    this.df.addSeries(DataFrame.COL_TIME, timestamps).setIndex(DataFrame.COL_TIME);
    this.df.addSeries(DataFrame.COL_VALUE, baselineValues);
  }

  public TimeSeries(LongSeries timestamps, DoubleSeries baselineValues, DoubleSeries currentValues,
      DoubleSeries upperBoundValues, DoubleSeries lowerBoundValues) {
    this(timestamps, baselineValues);
    this.df.addSeries(DataFrame.COL_CURRENT, currentValues);
    this.df.addSeries(COL_UPPER_BOUND, upperBoundValues);
    this.df.addSeries(COL_LOWER_BOUND, lowerBoundValues);
  }

  /**
   * the size of the time series
   *
   * @return the size of the time series (number of data points)
   */
  public int size() {
    return this.df.size();
  }

  /**
   * Add the series into TimeSeries if it exists in the DataFrame.
   *
   * @param df The source DataFrame.
   * @param name The series name.
   */
  private static void addSeries(TimeSeries ts, DataFrame df, String name) {
    if (df.contains(name)) {
      ts.df.addSeries(name, df.get(name));
    }
  }

  /**
   * return a empty time series
   *
   * @return a empty time series
   */
  public static TimeSeries empty() {
    TimeSeries ts = new TimeSeries();
    ts.df.addSeries(DataFrame.COL_TIME, LongSeries.empty())
        .addSeries(DataFrame.COL_VALUE, DoubleSeries.empty())
        .addSeries(DataFrame.COL_CURRENT, DoubleSeries.empty())
        .addSeries(COL_UPPER_BOUND, DoubleSeries.empty())
        .addSeries(COL_LOWER_BOUND, DoubleSeries.empty())
        .setIndex(DataFrame.COL_TIME);
    return ts;
  }

  /**
   * Add DataFrame into TimeSeries.
   *
   * @param df The source DataFrame.
   * @return TimeSeries that contains the predicted values.
   */
  public static TimeSeries fromDataFrame(DataFrame df) {
    Preconditions.checkArgument(df.contains(DataFrame.COL_TIME));
    Preconditions.checkArgument(df.contains(DataFrame.COL_VALUE));
    TimeSeries ts = new TimeSeries();
    // time stamp
    ts.df.addSeries(DataFrame.COL_TIME, df.get(DataFrame.COL_TIME)).setIndex(DataFrame.COL_TIME);
    // predicted baseline values
    addSeries(ts, df, DataFrame.COL_VALUE);
    // current values
    addSeries(ts, df, DataFrame.COL_CURRENT);
    // upper bound
    addSeries(ts, df, COL_UPPER_BOUND);
    // lower bound
    addSeries(ts, df, COL_LOWER_BOUND);
    return ts;
  }

  public DoubleSeries getCurrent() {
    return this.df.getDoubles(DataFrame.COL_CURRENT);
  }

  public LongSeries getTime() {
    return this.df.getLongs(DataFrame.COL_TIME);
  }

  public DoubleSeries getPredictedBaseline() {
    return this.df.getDoubles(DataFrame.COL_VALUE);
  }

  public boolean hasUpperBound() {
    return df.contains(COL_UPPER_BOUND);
  }

  public DoubleSeries getPredictedUpperBound() {
    return this.df.getDoubles(COL_UPPER_BOUND);
  }

  public boolean hasLowerBound() {
    return df.contains(COL_LOWER_BOUND);
  }

  public DoubleSeries getPredictedLowerBound() {
    return this.df.getDoubles(COL_LOWER_BOUND);
  }

  public DataFrame getDataFrame() {
    return df;
  }

  @Override
  public String toString() {
    return "TimeSeries{" + "df=" + df + '}';
  }
}
