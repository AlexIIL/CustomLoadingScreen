package alexiil.mods.load.baked.func.stack.op;

import java.util.Deque;

import alexiil.mods.load.baked.func.stack.BakedStackFunction;
import alexiil.mods.load.baked.func.stack.StackFunctionException;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackOperationLessOrEqual extends BakedStackFunction {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        Double one = popDouble(stack);
        Double two = popDouble(stack);
        stack.push((Boolean) (two <= one));
    }

    @Override
    public String toString() {
        return "Less or Equal [ 2(Double) <= 1(Double)] -> [(Boolean)]";
    }
}
