/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_ID_UNEXPECTED_AT_CREATION;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_ID;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_MISSING_NAME;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static ai.startree.thirdeye.spi.ThirdEyeStatus.ERR_UNKNOWN;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.util.ResourceUtils.ensure;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.ResourceUtils.ensureNull;
import static ai.startree.thirdeye.util.ResourceUtils.serverError;

import ai.startree.thirdeye.DaoFilterBuilder;
import ai.startree.thirdeye.RequestCache;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.accessControl.AccessType;
import ai.startree.thirdeye.spi.api.CountApi;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.AbstractManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.google.common.collect.ImmutableMap;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import javax.ws.rs.core.MultivaluedMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class CrudService<ApiT extends ThirdEyeCrudApi<ApiT>, DtoT extends AbstractDTO> {

  private static final Logger log = LoggerFactory.getLogger(CrudService.class);
  protected final AuthorizationManager authorizationManager;

  protected final AbstractManager<DtoT> dtoManager;
  protected final ImmutableMap<String, String> apiToIndexMap;

  public CrudService(final AuthorizationManager authorizationManager,
      final AbstractManager<DtoT> dtoManager,
      final ImmutableMap<String, String> apiToIndexMap) {
    this.authorizationManager = authorizationManager;
    this.dtoManager = dtoManager;
    this.apiToIndexMap = ImmutableMap.<String, String>builder()
        .put("id", "baseId")
        .putAll(apiToIndexMap)
        .build();
  }

  public ApiT get(
      final ThirdEyePrincipal principal,
      final Long id) {
    final DtoT dto = getDto(id);
    authorizationManager.ensureCanRead(principal, dto);

    final RequestCache cache = createRequestCache();
    return toApi(dto, cache);
  }

  public ApiT findByName(
      final ThirdEyePrincipal principal,
      final String name) {
    final RequestCache cache = createRequestCache();
    ensureExists(name, ERR_MISSING_NAME);

    /* If name column is mapped, use the mapping, else use 'name' */
    final String nameColumn = optional(apiToIndexMap.get("name")).orElse("name");
    final List<DtoT> byName = dtoManager.filter(new DaoFilter()
        .setPredicate(Predicate.EQ(nameColumn, name)));

    ensure(byName.size() > 0, ERR_OBJECT_DOES_NOT_EXIST, name);
    if (byName.size() > 1) {
      throw serverError(ERR_UNKNOWN, "Error. Multiple objects with name: " + name);
    }
    DtoT dtoT = byName.iterator().next();
    authorizationManager.ensureCanRead(principal, dtoT);
    return toApi(dtoT, cache);
  }

  public Stream<ApiT> list(
      final ThirdEyePrincipal principal,
      final MultivaluedMap<String, String> queryParameters
  ) {
    final List<DtoT> results = queryParameters.size() > 0
        ? dtoManager.filter(new DaoFilterBuilder(apiToIndexMap).buildFilter(queryParameters))
        : dtoManager.findAll();

    final RequestCache cache = createRequestCache();
    return results.stream()
        .filter(dto -> authorizationManager.hasAccess(principal, dto, AccessType.READ))
        .map(dto -> toApi(dto, cache));
  }

  @NonNull
  public List<ApiT> createMultiple(final ThirdEyePrincipal principal,
      final List<ApiT> list) {
    final RequestCache cache = createRequestCache();
    return list.stream()
        .peek(api1 -> validate(api1, null))
        .peek(api -> authorizationManager.ensureCanCreate(principal, toDto(api)))
        .map(api -> createDto(principal, api))
        .map(dto -> createGateKeeper(principal, dto))
        .peek(dtoManager::save)
        .peek(dto -> Objects.requireNonNull(dto.getId(), "DB update failed!"))
        .map(dto -> toApi(dto, cache))
        .collect(Collectors.toList());
  }

  @NonNull
  public List<ApiT> editMultiple(final ThirdEyePrincipal principal,
      final List<ApiT> list) {
    final RequestCache cache = createRequestCache();
    return list.stream()
        .map(o -> updateDto(principal, o))
        .peek(dtoManager::update)
        .peek(this::postUpdate)
        .map(dto -> toApi(dto, cache))
        .collect(Collectors.toList());
  }

  private DtoT updateDto(final ThirdEyePrincipal principal, final ApiT api) {
    final Long id = ensureExists(api.getId(), ERR_MISSING_ID);
    final DtoT existing = ensureExists(dtoManager.findById(id));
    validate(api, existing);

    final DtoT updated = toDto(api);

    // Check the that user can modify the resource before and after the update.
    authorizationManager.ensureCanEdit(principal, existing, updated);

    // Allow downstream classes to process any additional changes
    prepareUpdatedDto(principal, existing, updated);

    // Override system fields.
    updated.setId(id);
    updateGateKeeper(principal, existing, updated);

    // ready to be persisted to db.
    return updated;
  }

  /**
   * the PUT api performs an edit on an existing object by replacing its contents entirely.
   * Override this method to choose which fields need to carry forward from the old object to the
   * new. The system decides the update time, updatedBy, etc.
   *
   * For example, after edit,
   * - An alert might need to have a default cron or have the lastTimestamp copied over.
   *
   * @param principal user performing the update
   * @param existing the old object
   * @param updated the updated object (which is yet to be persisted)
   */
  protected void prepareUpdatedDto(
      final ThirdEyePrincipal principal,
      final DtoT existing,
      final DtoT updated) {
    // By default, do nothing.
  }

  private DtoT updateGateKeeper(final ThirdEyePrincipal principal,
      final DtoT existing,
      final DtoT updated) {
    updated.setCreatedBy(existing.getCreatedBy())
        .setCreateTime(existing.getCreateTime())
        .setUpdatedBy(principal.getName())
        .setUpdateTime(new Timestamp(new Date().getTime()));
    return updated;
  }

  public ApiT delete(final ThirdEyePrincipal principal, final Long id) {
    final DtoT dto = dtoManager.findById(id);
    if (dto != null) {
      authorizationManager.ensureCanDelete(principal, dto);

      deleteDto(dto);
      log.warn(String.format("Deleted id: %d by principal: %s", id, principal.getName()));

      final RequestCache cache = createRequestCache();
      return toApi(dto, cache);
    }

    return null;
  }

  public void deleteAll(final ThirdEyePrincipal principal) {
    dtoManager.findAll()
        .stream()
        .peek(dto -> authorizationManager.ensureCanDelete(principal, dto))
        .forEach(this::deleteDto);
  }

  public CountApi count(final MultivaluedMap<String, String> queryParameters) {
    final CountApi api = new CountApi();
    final Long count = queryParameters.size() > 0
        ? dtoManager.count(new DaoFilterBuilder(apiToIndexMap).buildFilter(queryParameters)
        .getPredicate())
        : dtoManager.count();
    api.setCount(count);
    return api;
  }

  /**
   * Initialize Request Cache
   *
   * @return cache container
   */
  protected RequestCache createRequestCache() {
    return new RequestCache();
  }

  /**
   * @param dto dto
   * @param cache optional cache parameter
   * @return convert to api object
   */
  protected ApiT toApi(final DtoT dto, final RequestCache cache) {
    return toApi(dto);
  }

  /**
   * No cache implementation
   *
   * @param dto dto
   * @return convert to api object
   */
  protected ApiT toApi(final DtoT dto) {
    throw new UnsupportedOperationException("Either of the 2 toApi variants must be implemented!");
  }

  protected DtoT toDto(final ApiT api) {
    throw new UnsupportedOperationException("Not implemented");
  }

  /**
   * Get the dto by id
   *
   * @param id id
   * @return dto
   */
  protected DtoT getDto(final Long id) {
    return ensureExists(dtoManager.findById(ensureExists(id, ERR_MISSING_ID)), "id");
  }

  /**
   * Validate create/edit. On Creation the existing dto will be null
   *
   * @param api The request api object
   * @param existing the existing dto. On Creation the existing dto will be null
   */
  protected void validate(final ApiT api, @Nullable final DtoT existing) {
    if (existing == null) {
      ensureNull(api.getId(), ERR_ID_UNEXPECTED_AT_CREATION);
    }
  }

  protected abstract DtoT createDto(final ThirdEyePrincipal principal, final ApiT api);

  private DtoT createGateKeeper(final ThirdEyePrincipal principal, final DtoT dto) {
    final Timestamp currentTime = new Timestamp(new Date().getTime());
    dto.setCreatedBy(principal.getName())
        .setCreateTime(currentTime)
        .setUpdatedBy(principal.getName())
        .setUpdateTime(currentTime);
    return dto;
  }

  protected void postUpdate(final DtoT dto) {
    // default is a no-op
  }

  protected void deleteDto(DtoT dto) {
    dtoManager.delete(dto);
  }
}
