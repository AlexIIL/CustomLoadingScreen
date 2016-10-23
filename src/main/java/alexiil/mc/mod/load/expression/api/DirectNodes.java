package alexiil.mc.mod.load.expression.api;

import alexiil.mc.mod.load.expression.api.IExpression.IExpressionBoolean;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionDouble;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionLong;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionString;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeLong;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;

public class DirectNodes {
    @FunctionalInterface
    public interface IDirectDouble extends INodeDouble {
        @Override
        default IDirectDouble inline(Arguments args) {
            return this;
        }
    }

    @FunctionalInterface
    public interface IDirectLong extends INodeLong {
        @Override
        default IDirectLong inline(Arguments args) {
            return this;
        }
    }

    @FunctionalInterface
    public interface IDirectBoolean extends INodeBoolean {
        @Override
        default IDirectBoolean inline(Arguments args) {
            return this;
        }
    }

    @FunctionalInterface
    public interface IDirectString extends INodeString {
        @Override
        default IDirectString inline(Arguments args) {
            return this;
        }
    }

    public static IExpressionDouble createExpression(IDirectDouble direct) {
        return new IExpressionDouble() {
            @Override
            public ArgumentCounts getCounts() {
                return ArgumentCounts.NO_ARGS;
            }

            @Override
            public IDirectDouble derive(Arguments args) {
                return direct;
            }
        };
    }

    public static IExpressionLong createExpression(IDirectLong direct) {
        return new IExpressionLong() {
            @Override
            public ArgumentCounts getCounts() {
                return ArgumentCounts.NO_ARGS;
            }

            @Override
            public IDirectLong derive(Arguments args) {
                return direct;
            }
        };
    }

    public static IExpressionBoolean createExpression(IDirectBoolean direct) {
        return new IExpressionBoolean() {
            @Override
            public ArgumentCounts getCounts() {
                return ArgumentCounts.NO_ARGS;
            }

            @Override
            public IDirectBoolean derive(Arguments args) {
                return direct;
            }
        };
    }

    public static IExpressionString createExpression(IDirectString direct) {
        return new IExpressionString() {
            @Override
            public ArgumentCounts getCounts() {
                return ArgumentCounts.NO_ARGS;
            }

            @Override
            public INodeString derive(Arguments args) {
                return direct;
            }
        };
    }
}
