package alexiil.mc.mod.load.expression.node.cast;

import alexiil.mc.mod.load.expression.NodeInliningHelper;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableDouble;

public class NodeCastLongToDouble implements INodeDouble {
    private final INodeLong from;

    public NodeCastLongToDouble(INodeLong from) {
        this.from = from;
    }

    @Override
    public double evaluate() {
        return from.evaluate();
    }

    @Override
    public INodeDouble inline(Arguments args) {
        return NodeInliningHelper.tryInline(this, args, from, (f) -> new NodeCastLongToDouble(f), (f) -> new NodeImmutableDouble(f.evaluate()));
    }

    @Override
    public String toString() {
        return "_long_to_double( " + from + " )";
    }
}
