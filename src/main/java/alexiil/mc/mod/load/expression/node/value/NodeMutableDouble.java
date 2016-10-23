package alexiil.mc.mod.load.expression.node.value;

import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;

public class NodeMutableDouble implements INodeDouble {
    public double value;

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
        return "mutable_double#" + System.identityHashCode(this);
    }
}
