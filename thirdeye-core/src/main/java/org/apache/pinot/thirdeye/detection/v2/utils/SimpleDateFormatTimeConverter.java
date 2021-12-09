package org.apache.pinot.thirdeye.detection.v2.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.pinot.thirdeye.spi.detection.TimeConverter;

// todo cyril this class is not used anymore - delete?
public class SimpleDateFormatTimeConverter implements TimeConverter {

  private final SimpleDateFormat sdf;

  public SimpleDateFormatTimeConverter(String timeFormat) {
    sdf = new SimpleDateFormat(timeFormat);
  }

  @Override
  public long convert(final String timeValue) {
    try {
      return sdf.parse(timeValue).getTime();
    } catch (ParseException e) {
      throw new RuntimeException(
          "Unable to parse time value " + timeValue, e);
    }
  }

  @Override
  public String convertMillis(final long time) {
    return sdf.format(new Date(time));
  }
}
