/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.notification;

import ai.startree.thirdeye.spi.detection.ConfigUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The detection config formatter
 */
public class DetectionConfigFormatter {

  private static final String PROP_METRIC_URNS_KEY = "metricUrn";
  private static final String PROP_NESTED_METRIC_URNS_KEY = "nestedMetricUrns";
  private static final String PROP_NESTED_PROPERTIES_KEY = "nested";
  /**
   * Extract the list of metric urns in the detection config properties
   *
   * @param properties the detection config properties
   * @return the list of metric urns
   */
  public static Set<String> extractMetricUrnsFromProperties(Map<String, Object> properties) {
    Set<String> metricUrns = new HashSet<>();
    if (properties == null) {
      return metricUrns;
    }
    if (properties.containsKey(PROP_METRIC_URNS_KEY)) {
      metricUrns.add((String) properties.get(PROP_METRIC_URNS_KEY));
    }
    if (properties.containsKey(PROP_NESTED_METRIC_URNS_KEY)) {
      metricUrns.addAll(ConfigUtils.getList(properties.get(PROP_NESTED_METRIC_URNS_KEY)));
    }
    List<Map<String, Object>> nestedProperties = ConfigUtils
        .getList(properties.get(PROP_NESTED_PROPERTIES_KEY));
    // extract the metric urns recursively from the nested properties
    for (Map<String, Object> nestedProperty : nestedProperties) {
      metricUrns.addAll(extractMetricUrnsFromProperties(nestedProperty));
    }
    return metricUrns;
  }
}
