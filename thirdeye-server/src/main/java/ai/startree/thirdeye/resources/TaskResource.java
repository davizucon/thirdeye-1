/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.mapper.ApiBeanMapper;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.TaskApi;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.TaskManager;
import ai.startree.thirdeye.spi.datalayer.dto.TaskDTO;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.auth.Auth;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiKeyAuthDefinition;
import io.swagger.annotations.ApiKeyAuthDefinition.ApiKeyLocation;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api(tags = "Task", authorizations = {@Authorization(value = "oauth")})
@SwaggerDefinition(securityDefinition = @SecurityDefinition(apiKeyAuthDefinitions = @ApiKeyAuthDefinition(name = HttpHeaders.AUTHORIZATION, in = ApiKeyLocation.HEADER, key = "oauth")))
@Singleton
@Produces(MediaType.APPLICATION_JSON)
public class TaskResource extends CrudResource<TaskApi, TaskDTO> {

  public static final ImmutableMap<String, String> API_TO_INDEX_FILTER_MAP = ImmutableMap.<String, String>builder()
      .put("type", "type")
      .put("status", "status")
      .put("created", "createTime")
      .put("updated", "updateTime")
      .build();

  @Inject
  public TaskResource(final TaskManager taskManager) {
    super(taskManager, API_TO_INDEX_FILTER_MAP);
  }

  // Operation not supported to prevent create of tasks
  @Override
  protected TaskDTO createDto(final ThirdEyePrincipal principal, final TaskApi taskApi) {
    throw new UnsupportedOperationException();
  }

  @Override
  protected TaskApi toApi(final TaskDTO dto) {
    return ApiBeanMapper.toApi(dto);
  }

  // Overridden to disable endpoint
  @Override
  @POST
  @ApiOperation(value = "", hidden = true)
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response createMultiple(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      final List<TaskApi> list) {
    throw new UnsupportedOperationException();
  }

  // Overridden to disable endpoint
  @Override
  @PUT
  @ApiOperation(value = "", hidden = true)
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response editMultiple(
      @ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      final List<TaskApi> list) {
    throw new UnsupportedOperationException();
  }

  @DELETE
  @Path("/purge")
  @Timed
  @Produces(MediaType.APPLICATION_JSON)
  public Response cleanUp(@ApiParam(hidden = true) @Auth final ThirdEyePrincipal principal,
      @ApiParam(value = "Older than (number of days)", defaultValue = "60") @QueryParam("olderThanInDays") @NotNull Integer nDays,
      @ApiParam(value = "Max Entries to delete", defaultValue = "10000") @QueryParam("limit") @NotNull Integer limitOptional
  ) {
    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    final int nDaysToDelete = optional(nDays).orElse(60);
    final long twoMonthsBack = System.currentTimeMillis() - Duration.ofDays(nDaysToDelete).toMillis();
    final String formattedDate = df.format(new Date(twoMonthsBack));

    final int limit = optional(limitOptional).orElse(10000);
    dtoManager
        .filter(new DaoFilter()
            .setPredicate(Predicate.LT("createTime", formattedDate))
            .setLimit(limit)
        )
        .forEach(this::deleteDto);
    return Response.ok().build();
  }
}
