package alexiil.mc.mod.load.expression.node.cast;

import alexiil.mc.mod.load.expression.NodeInliningHelper;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableString;

public class NodeCastBooleanToString implements INodeString {
    private final INodeBoolean from;

    public NodeCastBooleanToString(INodeBoolean from) {
        this.from = from;
    }

    @Override
    public String evaluate() {
        return Boolean.toString(from.evaluate());
    }

    @Override
    public INodeString inline(Arguments args) {
        return NodeInliningHelper.tryInline(this, args, from, (f) -> new NodeCastBooleanToString(f), (f) -> new NodeImmutableString(Boolean.toString(f.evaluate())));
    }

    @Override
    public String toString() {
        return "_to_string(" + from + ")";
    }
}
