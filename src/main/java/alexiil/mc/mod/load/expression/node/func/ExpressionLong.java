package alexiil.mc.mod.load.expression.node.func;

import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.api.ArgumentCounts;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionLong;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeLong;

public class ExpressionLong implements IExpressionLong {
    private final INodeLong node;
    private final ArgumentCounts counts;

    public ExpressionLong(INodeLong node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    @Override
    public ArgumentCounts getCounts() {
        return counts;
    }

    @Override
    public INodeLong derive(Arguments args) {
        GenericExpressionCompiler.debugStart("Deriving from " + args);
        INodeLong n = node.inline(args);
        GenericExpressionCompiler.debugEnd("Derived as " + n);
        return n;
    }

    @Override
    public String toString() {
        return counts + " -> ( " + node + " )";
    }
}
