package alexiil.mc.mod.load.expression.node.value;

import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeLong;

public class NodeMutableLong implements INodeLong {
    public long value;

    @Override
    public long evaluate() {
        return value;
    }

    @Override
    public INodeLong inline(Arguments args) {
        return this;
    }

    @Override
    public String toString() {
        return "mutable_long#" + System.identityHashCode(this);
    }
}
