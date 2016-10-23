package alexiil.mc.mod.load.expression.node.func;

import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.api.ArgumentCounts;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionDouble;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;

public class ExpressionDouble implements IExpressionDouble {
    private final INodeDouble node;
    private final ArgumentCounts counts;

    public ExpressionDouble(INodeDouble node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    @Override
    public ArgumentCounts getCounts() {
        return counts;
    }

    @Override
    public INodeDouble derive(Arguments args) {
        GenericExpressionCompiler.debugStart("Deriving from " + args);
        INodeDouble n = node.inline(args);
        GenericExpressionCompiler.debugEnd("Derived as " + n);
        return n;
    }

    @Override
    public String toString() {
        return counts + " -> ( " + node + " )";
    }
}
