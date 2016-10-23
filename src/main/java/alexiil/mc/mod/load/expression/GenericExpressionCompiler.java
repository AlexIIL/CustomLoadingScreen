package alexiil.mc.mod.load.expression;

import org.apache.commons.lang3.tuple.Pair;

import alexiil.mc.mod.load.expression.api.ArgumentCounts;
import alexiil.mc.mod.load.expression.api.IExpression;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionBoolean;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionDouble;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionLong;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionString;
import alexiil.mc.mod.load.expression.api.IExpressionNode;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeLong;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;
import alexiil.mc.mod.load.expression.node.cast.NodeCasting;
import alexiil.mc.mod.load.expression.node.func.ExpressionBoolean;
import alexiil.mc.mod.load.expression.node.func.ExpressionDouble;
import alexiil.mc.mod.load.expression.node.func.ExpressionLong;
import alexiil.mc.mod.load.expression.node.func.ExpressionString;

public class GenericExpressionCompiler {

    public static IExpressionLong compileExpressionLong(String function) throws InvalidExpressionException {
        return compileExpressionLong(function, null);
    }

    public static IExpressionLong compileExpressionLong(String function, FunctionContext functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);

        if (exp.getLeft() instanceof INodeLong) {
            return new ExpressionLong((INodeLong) exp.getLeft(), exp.getRight());
        }

        throw new InvalidExpressionException("Not a long " + exp.getLeft());
    }

    public static IExpressionDouble compileExpressionDouble(String function) throws InvalidExpressionException {
        return compileExpressionDouble(function, null);
    }

    public static IExpressionDouble compileExpressionDouble(String function, FunctionContext functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);
        INodeDouble nodeDouble = NodeCasting.castToDouble(exp.getLeft());
        return new ExpressionDouble(nodeDouble, exp.getRight());
    }

    public static IExpressionBoolean compileExpressionBoolean(String function) throws InvalidExpressionException {
        return compileExpressionBoolean(function, null);
    }

    public static IExpressionBoolean compileExpressionBoolean(String function, FunctionContext functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);

        if (exp.getLeft() instanceof INodeBoolean) {
            return new ExpressionBoolean((INodeBoolean) exp.getLeft(), exp.getRight());
        }

        throw new InvalidExpressionException("Not a boolean " + exp.getLeft());
    }

    public static IExpressionString compileExpressionString(String function) throws InvalidExpressionException {
        return compileExpressionString(function, null);
    }

    public static IExpressionString compileExpressionString(String function, FunctionContext functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);
        INodeString nodeString = NodeCasting.castToString(exp.getLeft());
        return new ExpressionString(nodeString, exp.getRight());
    }

    public static IExpression compileExpressionUnknown(String function) throws InvalidExpressionException {
        return compileExpressionUnknown(function, null);
    }

    public static IExpression compileExpressionUnknown(String function, FunctionContext functions) throws InvalidExpressionException {
        Pair<IExpressionNode, ArgumentCounts> exp = compileExpression(function, functions);
        IExpressionNode node = exp.getLeft();
        if (node instanceof INodeString) {
            return new ExpressionString((INodeString) node, exp.getRight());
        }
        if (node instanceof INodeBoolean) {
            return new ExpressionBoolean((INodeBoolean) node, exp.getRight());
        }
        if (node instanceof INodeLong) {
            return new ExpressionLong((INodeLong) node, exp.getRight());
        }
        if (node instanceof INodeDouble) {
            return new ExpressionDouble((INodeDouble) node, exp.getRight());
        }
        throw new InvalidExpressionException("Unknown node type " + node);
    }

    private static Pair<IExpressionNode, ArgumentCounts> compileExpression(String function, FunctionContext context) throws InvalidExpressionException {
        return InternalCompiler.compileExpression(function, context);
    }

    public static void debugStart(String text) {}

    public static void debugEnd(String text) {}

    public static void debugPrintln(String text) {}
}
