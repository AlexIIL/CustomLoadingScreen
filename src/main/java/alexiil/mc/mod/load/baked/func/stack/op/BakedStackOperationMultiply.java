package alexiil.mc.mod.load.baked.func.stack.op;

import java.util.Deque;

import alexiil.mc.mod.load.baked.func.stack.BakedStackFunction;
import alexiil.mc.mod.load.baked.func.stack.StackFunctionException;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedStackOperationMultiply extends BakedStackFunction {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        double one = popDouble(stack);
        double two = popDouble(stack);
        stack.push(one * two);
    }

    @Override
    public String toString() {
        return "Multiply [ 1(Double) * 2(Double) ] -> [(Double)]";
    }
}
