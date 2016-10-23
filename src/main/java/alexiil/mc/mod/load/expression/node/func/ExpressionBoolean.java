package alexiil.mc.mod.load.expression.node.func;

import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.api.ArgumentCounts;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionBoolean;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;

public class ExpressionBoolean implements IExpressionBoolean {
    private final INodeBoolean node;
    private final ArgumentCounts counts;

    public ExpressionBoolean(INodeBoolean node, ArgumentCounts counts) {
        this.node = node;
        this.counts = counts;
    }

    @Override
    public ArgumentCounts getCounts() {
        return counts;
    }

    @Override
    public INodeBoolean derive(Arguments args) {
        GenericExpressionCompiler.debugStart("Deriving from " + args);
        INodeBoolean n = node.inline(args);
        GenericExpressionCompiler.debugEnd("Derived as " + n);
        return n;
    }

    @Override
    public String toString() {
        return counts + " -> ( " + node + " )";
    }
}
