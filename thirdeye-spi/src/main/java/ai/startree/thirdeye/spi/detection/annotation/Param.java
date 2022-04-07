/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Param {

  @JsonProperty String name() default "";

  @JsonProperty String placeholder() default "";

  @JsonProperty String defaultValue() default "";

  @JsonProperty String[] allowableValues() default {};

  @JsonProperty boolean required() default false;

  @JsonProperty String description() default "";
}
