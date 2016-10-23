package alexiil.mc.mod.load.expression.node.unary;

import java.util.function.DoubleUnaryOperator;

import alexiil.mc.mod.load.expression.NodeInliningHelper;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableDouble;

public class NodeUnaryDouble implements INodeDouble {
    public enum Type {
        NEG("-", (v) -> -v);

        private final String op;
        private final DoubleUnaryOperator operator;

        Type(String op, DoubleUnaryOperator operator) {
            this.op = op;
            this.operator = operator;
        }

        public NodeUnaryDouble create(INodeDouble from) {
            return new NodeUnaryDouble(from, this);
        }
    }

    private final INodeDouble from;
    private final Type type;

    private NodeUnaryDouble(INodeDouble from, Type type) {
        this.from = from;
        this.type = type;
    }

    @Override
    public double evaluate() {
        return type.operator.applyAsDouble(from.evaluate());
    }

    @Override
    public INodeDouble inline(Arguments args) {
        return NodeInliningHelper.tryInline(this, args, from, //
                (f) -> new NodeUnaryDouble(f, type),//
                (f) -> new NodeImmutableDouble(type.operator.applyAsDouble(f.evaluate())));
    }

    @Override
    public String toString() {
        return type.op + "(" + from + ")";
    }
}
