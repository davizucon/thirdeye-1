package org.apache.pinot.thirdeye.alert;

import static java.util.Collections.emptyList;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.pinot.thirdeye.detection.v2.plan.PlanNodeFactory;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean;
import org.apache.pinot.thirdeye.spi.datalayer.dto.PlanNodeBean.InputBean;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;

@Singleton
public class PlanExecutor {

  public static final String ROOT_OPERATOR_KEY = "root";

  private final PlanNodeFactory planNodeFactory;

  @Inject
  public PlanExecutor(final PlanNodeFactory planNodeFactory) {
    this.planNodeFactory = planNodeFactory;
  }

  /**
   * template is not populated for the legacy detection pipeline.
   *
   * @param alert the alert DTO
   * @return true if this is a v2 alert.
   */
  public static boolean isV2Alert(final AlertDTO alert) {
    return alert.getTemplate() != null;
  }

  @VisibleForTesting
  static void executePlanNode(final Map<String, PlanNode> pipelinePlanNodes,
      final Map<ContextKey, DetectionPipelineResult> context,
      final PlanNode node)
      throws Exception {
    for (final InputBean input : optional(node.getPlanNodeInputs()).orElse(emptyList())) {
      final ContextKey contextKey = key(input.getSourcePlanNode(), input.getSourceProperty());
      if (!context.containsKey(contextKey)) {
        final PlanNode inputPlanNode = pipelinePlanNodes.get(input.getSourcePlanNode());
        executePlanNode(pipelinePlanNodes, context, inputPlanNode);
      }
      if (!context.containsKey(contextKey)) {
        throw new RuntimeException("Missing context key - " + contextKey);
      }
      node.setInput(input.getTargetProperty(), context.get(contextKey));
    }
    final Operator operator = node.buildOperator();
    operator.execute();
    final Map<String, DetectionPipelineResult> outputs = operator.getOutputs();
    for (final Entry<String, DetectionPipelineResult> output : outputs.entrySet()) {
      context.put(key(node.getName(), output.getKey()), output.getValue());
    }
  }

  @VisibleForTesting
  static ContextKey key(final String name, final String key) {
    return new ContextKey(name, key);
  }

  private static Map<String, DetectionPipelineResult> getOutput(
      final Map<ContextKey, DetectionPipelineResult> context, final String nodeName) {
    final Map<String, DetectionPipelineResult> results = new HashMap<>();
    for (final ContextKey contextKey : context.keySet()) {
      if (contextKey.getNodeName().equals(nodeName)) {
        results.put(contextKey.getKey(), context.get(contextKey));
      }
    }
    return results;
  }

  /**
   * Main interface for running the pipeline.
   *
   * @param planNodeBeans The pipeline DAG as a list of nodes
   * @return The result map
   * @throws Exception All exceptions are to be handled by upstream consumer.
   */
  public Map<String, DetectionPipelineResult> runPipeline(final List<PlanNodeBean> planNodeBeans,
      final long startTime,
      final long endTime)
      throws Exception {
    /* map of all the plan nodes constructed from beans(persisted objects) */
    final Map<String, PlanNode> pipelinePlanNodes = buildPlanNodeMap(
        planNodeBeans,
        startTime,
        endTime);

    /* The context stores all the outputs from all the nodes */
    final Map<ContextKey, DetectionPipelineResult> context = new HashMap<>();

    /* Execute the DAG */
    final PlanNode rootNode = pipelinePlanNodes.get(ROOT_OPERATOR_KEY);
    executePlanNode(pipelinePlanNodes, context, rootNode);

    /* Return the output */
    return getOutput(context, rootNode.getName());
  }

  @VisibleForTesting
  Map<String, PlanNode> buildPlanNodeMap(final List<PlanNodeBean> planNodeBeans,
      final long startTime, final long endTime) {
    final Map<String, PlanNode> pipelinePlanNodes = new HashMap<>();
    for (final PlanNodeBean planNodeBean : planNodeBeans) {
      final PlanNode planNode = planNodeFactory.build(
          planNodeBean,
          startTime,
          endTime,
          pipelinePlanNodes
      );

      pipelinePlanNodes.put(planNodeBean.getName(), planNode);
    }
    return pipelinePlanNodes;
  }
}
