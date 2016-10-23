package alexiil.mc.mod.load.expression.node.func;

import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.api.ArgumentCounts;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionString;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;

public class ExpressionString implements IExpressionString {
    private final INodeString node;
    private final ArgumentCounts counts;

    public ExpressionString(INodeString node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    @Override
    public ArgumentCounts getCounts() {
        return counts;
    }

    @Override
    public INodeString derive(Arguments args) {
        GenericExpressionCompiler.debugStart("Deriving from " + args);
        INodeString n = node.inline(args);
        GenericExpressionCompiler.debugEnd("Derived as " + n);
        return n;
    }

    @Override
    public String toString() {
        return counts + " -> ( " + node + " )";
    }
}
