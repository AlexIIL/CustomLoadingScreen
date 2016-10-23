package alexiil.mc.mod.load.expression.node.cast;

import alexiil.mc.mod.load.expression.InvalidExpressionException;
import alexiil.mc.mod.load.expression.api.IExpressionNode;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeLong;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;

public class NodeCasting {
    public static INodeString castToString(IExpressionNode node) {
        if (node instanceof INodeString) {
            return (INodeString) node;
        }

        if (node instanceof INodeBoolean) {
            return new NodeCastBooleanToString((INodeBoolean) node);
        }

        if (node instanceof INodeLong) {
            return new NodeCastLongToString((INodeLong) node);
        }

        if (node instanceof INodeDouble) {
            return new NodeCastDoubleToString((INodeDouble) node);
        }

        throw new IllegalStateException("Unknonw node type " + node.getClass());
    }

    public static INodeDouble castToDouble(IExpressionNode node) throws InvalidExpressionException {
        if (node instanceof INodeDouble) {
            return (INodeDouble) node;
        }

        if (node instanceof INodeLong) {
            return new NodeCastLongToDouble((INodeLong) node);
        }

        throw new InvalidExpressionException("Cannot cast " + node + " to a double!");
    }
}
