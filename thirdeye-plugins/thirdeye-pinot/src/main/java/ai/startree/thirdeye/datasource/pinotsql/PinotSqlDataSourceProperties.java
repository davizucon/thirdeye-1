/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datasource.pinotsql;

public enum PinotSqlDataSourceProperties {
  CONTROLLER_HOST("controllerHost"),
  CONTROLLER_PORT("controllerPort"),
  CONTROLLER_CONNECTION_SCHEME("controllerConnectionScheme"),
  CONTROLLER_HEADERS("headers");

  private final String value;

  PinotSqlDataSourceProperties(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}

