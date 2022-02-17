/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource;

import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.detection.TimeSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelationalThirdEyeResponse extends BaseThirdEyeResponse {

  private final Map<MetricFunction, Integer> metricFuncToIdMapping;
  private final List<ThirdEyeResponseRow> responseRows;
  private final List<String[]> rows;

  public RelationalThirdEyeResponse(ThirdEyeRequest request, List<String[]> rows,
      TimeSpec dataTimeSpec) {
    super(request, dataTimeSpec);
    this.rows = rows;
    this.responseRows = new ArrayList<>(rows.size());
    this.metricFuncToIdMapping = new HashMap<>();
    for (int i = 0; i < metricFunctions.size(); i++) {
      metricFuncToIdMapping.put(metricFunctions.get(i), i + groupKeyColumns.size());
    }
    for (String[] row : rows) {
      int timeBucketId = -1;
      List<String> dimensions = new ArrayList<>();
      if (!groupKeyColumns.isEmpty()) {
        for (int i = 0; i < groupKeyColumns.size(); i++) {
          if (request.getGroupByTimeGranularity() != null && i == 0) {
            timeBucketId = Integer.parseInt(row[i]);
          } else {
            dimensions.add(row[i]);
          }
        }
      }
      List<Double> metrics = new ArrayList<>();
      for (int i = 0; i < metricFunctions.size(); i++) {
        metrics.add(Double.parseDouble(row[groupKeyColumns.size() + i]));
      }
      ThirdEyeResponseRow responseRow = new ThirdEyeResponseRow(timeBucketId, dimensions, metrics);
      responseRows.add(responseRow);
    }
  }

  @Override
  public int getNumRowsFor(MetricFunction metricFunction) {
    return rows.size();
  }

  @Override
  public Map<String, String> getRow(MetricFunction metricFunction, int rowId) {
    Map<String, String> rowMap = new HashMap<>();
    String[] rowValues = rows.get(rowId);
    for (int i = 0; i < groupKeyColumns.size(); i++) {
      String groupByKey = groupKeyColumns.get(i);
      rowMap.put(groupByKey, rowValues[i]);
    }
    rowMap.put(metricFunction.toString(), rowValues[metricFuncToIdMapping.get(metricFunction)]);
    rowMap.put(Constants.TIMESTAMP, rowValues[rowValues.length - 1]);
    return rowMap;
  }

  @Override
  public int getNumRows() {
    return rows.size();
  }

  @Override
  public ThirdEyeResponseRow getRow(int rowId) {
    return responseRows.get(rowId);
  }
}
