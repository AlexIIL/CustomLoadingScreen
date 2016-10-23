package alexiil.mc.mod.load.expression.api;

import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeLong;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;

public interface IExpression {
    ArgumentCounts getCounts();

    IExpressionNode derive(Arguments args);

    public interface IExpressionLong extends IExpression {
        @Override
        INodeLong derive(Arguments args);
    }

    public interface IExpressionDouble extends IExpression {
        @Override
        INodeDouble derive(Arguments args);
    }

    public interface IExpressionBoolean extends IExpression {
        @Override
        INodeBoolean derive(Arguments args);
    }

    public interface IExpressionString extends IExpression {
        @Override
        INodeString derive(Arguments args);
    }
}
