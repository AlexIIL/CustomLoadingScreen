package alexiil.mc.mod.load.expression.node.value;

import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;

public class NodeMutableBoolean implements INodeBoolean {
    public boolean value;

    @Override
    public boolean evaluate() {
        return value;
    }

    @Override
    public INodeBoolean inline(Arguments args) {
        return this;
    }

    @Override
    public String toString() {
        return "mutable_boolean#" + System.identityHashCode(this);
    }
}
