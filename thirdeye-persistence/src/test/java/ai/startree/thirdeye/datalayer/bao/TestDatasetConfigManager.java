/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.DatalayerTestUtils;
import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestDatasetConfigManager {

  private static final String collection1 = "my dataset1";
  private static final String collection2 = "my dataset2";

  private Long datasetConfigId1;
  private Long datasetConfigId2;
  private DatasetConfigManager datasetConfigDAO;

  @BeforeClass
  void beforeClass() {
    datasetConfigDAO = new TestDatabase().createInjector().getInstance(DatasetConfigManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {

  }

  @Test
  public void testCreate() {

    DatasetConfigDTO datasetConfig1 = DatalayerTestUtils.getTestDatasetConfig(collection1);
    datasetConfigId1 = datasetConfigDAO.save(datasetConfig1);
    Assert.assertNotNull(datasetConfigId1);

    DatasetConfigDTO datasetConfig2 = DatalayerTestUtils.getTestDatasetConfig(collection2);
    datasetConfig2.setActive(false);
    datasetConfigId2 = datasetConfigDAO.save(datasetConfig2);
    Assert.assertNotNull(datasetConfigId2);

    List<DatasetConfigDTO> datasetConfigs = datasetConfigDAO.findAll();
    Assert.assertEquals(datasetConfigs.size(), 2);

    datasetConfigs = datasetConfigDAO.findActive();
    Assert.assertEquals(datasetConfigs.size(), 1);
  }

  @Test(dependsOnMethods = {"testCreate"})
  public void testFindByDataset() {
    DatasetConfigDTO datasetConfigs = datasetConfigDAO.findByDataset(collection1);
    Assert.assertEquals(datasetConfigs.getDataset(), collection1);
  }

  @Test(dependsOnMethods = {"testFindByDataset"})
  public void testUpdate() {
    DatasetConfigDTO datasetConfig = datasetConfigDAO.findById(datasetConfigId1);
    Assert.assertNotNull(datasetConfig);
    Assert.assertFalse(datasetConfig.isRealtime());
    datasetConfig.setRealtime(true);
    datasetConfigDAO.update(datasetConfig);
    datasetConfig = datasetConfigDAO.findById(datasetConfigId1);
    Assert.assertNotNull(datasetConfig);
    Assert.assertTrue(datasetConfig.isRealtime());
  }

  @Test(dependsOnMethods = {"testUpdate"})
  public void testDelete() {
    datasetConfigDAO.deleteById(datasetConfigId2);
    DatasetConfigDTO datasetConfig = datasetConfigDAO.findById(datasetConfigId2);
    Assert.assertNull(datasetConfig);
  }
}
