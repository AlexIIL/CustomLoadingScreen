package alexiil.mc.mod.load.expression.node.value;

import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;

public class NodeImmutableDouble implements INodeDouble, IImmutableNode {
    public final double value;

    public NodeImmutableDouble(double value) {
        this.value = value;
    }

    @Override
    public double evaluate() {
        return value;
    }

    @Override
    public INodeDouble inline(Arguments args) {
        return this;
    }

    @Override
    public String toString() {
        return Double.toString(value) + "D";
    }
}
