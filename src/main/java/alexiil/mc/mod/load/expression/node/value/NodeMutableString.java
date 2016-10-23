package alexiil.mc.mod.load.expression.node.value;

import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;

public class NodeMutableString implements INodeString {
    public String value = "";

    @Override
    public String evaluate() {
        return value;
    }

    @Override
    public INodeString inline(Arguments args) {
        return this;
    }

    @Override
    public String toString() {
        return "mutable_string#" + System.identityHashCode(this);
    }
}
