/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.cube.data.node;

import java.util.List;
import org.apache.commons.lang3.ObjectUtils;

public class CubeNodeUtils {

  /**
   * Check if the hierarchical tree of the given two root nodes are the same.
   *
   * @param node1 the root node of the first hierarchical tree.
   * @param node2 the root node of the second hierarchical tree.
   * @return true if both hierarchical tree are the same.
   */
  public static boolean equalHierarchy(CubeNode node1, CubeNode node2) {
    return equalHierarchy(node1, null, node2, null);
  }

  public static boolean equalHierarchy(CubeNode node1, CubeNode node1Parent, CubeNode node2,
      CubeNode node2Parent) {
    if (!ObjectUtils.equals(node1, node2)) { // Return false if data of the nodes are different.
      return false;
    } else { // Check hierarchy if the two given nodes have the same data value.
      // Check parent reference
      if (node1Parent != null && node1.getParent() != node1Parent) {
        return false;
      }
      if (node2Parent != null && node2.getParent() != node2Parent) {
        return false;
      }

      // Check children reference
      if (node1.childrenSize() != node2.childrenSize()) {
        return false;
      }
      List<CubeNode> children1 = node1.getChildren();
      List<CubeNode> children2 = node2.getChildren();
      int size = children1.size();
      for (int i = 0; i < size; i++) {
        CubeNode child1 = children1.get(i);
        CubeNode child2 = children2.get(i);
        boolean sameChild = equalHierarchy(child1, node1, child2, node2);
        if (!sameChild) {
          return false;
        }
      }
      return true;
    }
  }
}
