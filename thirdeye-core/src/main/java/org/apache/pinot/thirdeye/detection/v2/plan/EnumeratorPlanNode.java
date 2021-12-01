package org.apache.pinot.thirdeye.detection.v2.plan;

import static java.util.Collections.emptyMap;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.EnumeratorOperator;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNodeContext;

public class EnumeratorPlanNode extends DetectionPipelinePlanNode {

  public static final String TYPE = "Enumerator";
  private Map<String, Object> params;

  public EnumeratorPlanNode() {
    super();
  }

  @Override
  public void init(final PlanNodeContext planNodeContext) {
    super.init(planNodeContext);
    params = optional(planNodeBean.getParams()).orElse(emptyMap());
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public Map<String, Object> getParams() {
    return params;
  }

  @Override
  public Operator buildOperator() throws Exception {
    final EnumeratorOperator operator = new EnumeratorOperator();
    operator.init(new OperatorContext()
        .setStartTime(params
            .getOrDefault("startTime", String.valueOf(this.startTime))
            .toString())
        .setEndTime(params
            .getOrDefault("endTime", String.valueOf(this.endTime))
            .toString())
        .setInputsMap(inputsMap)
        .setPlanNode(planNodeBean)
    );
    return operator;
  }
}
