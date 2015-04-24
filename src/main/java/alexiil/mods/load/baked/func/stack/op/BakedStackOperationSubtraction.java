package alexiil.mods.load.baked.func.stack.op;

import java.util.Deque;

import alexiil.mods.load.baked.func.stack.BakedStackFunction;
import alexiil.mods.load.baked.func.stack.StackFunctionException;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackOperationSubtraction extends BakedStackFunction {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        double one = popDouble(stack);
        double two = popDouble(stack);
        stack.push(two - one);
    }

    @Override
    public String toString() {
        return "Subtraction [ 2(Double) - 1(Double) ] -> [(Double)]";
    }
}
