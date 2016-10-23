package alexiil.mc.mod.load.expression.node.value;

import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;

public class NodeImmutableString implements INodeString, IImmutableNode {
    public final String value;

    public NodeImmutableString(String value) {
        this.value = value;
    }

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
        return "'" + value + "'";
    }
}
