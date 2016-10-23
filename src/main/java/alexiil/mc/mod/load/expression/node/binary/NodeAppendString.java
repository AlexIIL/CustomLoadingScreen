package alexiil.mc.mod.load.expression.node.binary;

import alexiil.mc.mod.load.expression.NodeInliningHelper;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableString;

public class NodeAppendString implements INodeString {
    private final INodeString left, right;

    public NodeAppendString(INodeString left, INodeString right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String evaluate() {
        return left.evaluate() + right.evaluate();
    }

    @Override
    public INodeString inline(Arguments args) {
        return NodeInliningHelper.tryInline(this, args, left, right, //
                (l, r) -> new NodeAppendString(l, r), //
                (l, r) -> new NodeImmutableString(l.evaluate() + r.evaluate()));
    }

    @Override
    public String toString() {
        return "(" + left + ") + (" + right + ")";
    }
}
