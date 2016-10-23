package alexiil.mc.mod.load.expression.node.cast;

import alexiil.mc.mod.load.expression.NodeInliningHelper;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableString;

public class NodeCastDoubleToString implements INodeString {
    private final INodeDouble from;

    public NodeCastDoubleToString(INodeDouble from) {
        this.from = from;
    }

    @Override
    public String evaluate() {
        return Double.toString(from.evaluate());
    }

    @Override
    public INodeString inline(Arguments args) {
        return NodeInliningHelper.tryInline(this, args, from, (f) -> new NodeCastDoubleToString(f), (f) -> new NodeImmutableString(Double.toString(f.evaluate())));
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
