package alexiil.mc.mod.load.expression.node.cast;

import alexiil.mc.mod.load.expression.NodeInliningHelper;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableString;

public class NodeCastLongToString implements INodeString {
    private final INodeLong from;

    public NodeCastLongToString(INodeLong from) {
        this.from = from;
    }

    @Override
    public String evaluate() {
        return Long.toString(from.evaluate());
    }

    @Override
    public INodeString inline(Arguments args) {
        return NodeInliningHelper.tryInline(this, args, from, (f) -> new NodeCastLongToString(f), (f) -> new NodeImmutableString(Long.toString(f.evaluate())));
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
