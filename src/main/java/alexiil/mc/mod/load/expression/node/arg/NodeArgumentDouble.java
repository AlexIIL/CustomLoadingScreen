package alexiil.mc.mod.load.expression.node.arg;

import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;

public class NodeArgumentDouble implements INodeDouble {
    private final int index;

    public NodeArgumentDouble(int index) {
        this.index = index;
    }

    @Override
    public double evaluate() {
        throw new IllegalStateException("Cannot evaluate without optimizing!");
    }

    @Override
    public INodeDouble inline(Arguments args) {
        if (args == null) {
            return this;
        }
        return args.doubles[index];
    }

    @Override
    public String toString() {
        return "Argument(Double)#" + index;
    }
}
