package alexiil.mc.mod.load.expression.node.unary;

import alexiil.mc.mod.load.expression.NodeInliningHelper;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableBoolean;

public class NodeBooleanInvert implements INodeBoolean {
    private final INodeBoolean from;

    public NodeBooleanInvert(INodeBoolean from) {
        this.from = from;
    }

    @Override
    public boolean evaluate() {
        return !from.evaluate();
    }

    @Override
    public INodeBoolean inline(Arguments args) {
        return NodeInliningHelper.tryInline(this, args, from, (f) -> new NodeBooleanInvert(f), (f) -> NodeImmutableBoolean.get(!f.evaluate()));
    }

    @Override
    public String toString() {
        return "!(" + from + ")";
    }
}
