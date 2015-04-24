package alexiil.mods.load.baked.func.stack.op;

import java.util.Deque;

import alexiil.mods.load.baked.func.stack.BakedStackFunction;
import alexiil.mods.load.baked.func.stack.StackFunctionException;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackOperationAnd extends BakedStackFunction {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        boolean one = popBoolean(stack);
        boolean two = popBoolean(stack);
        stack.push(one && two);
    }

    @Override
    public String toString() {
        return "And [ 1(Boolean) && 2(Boolean) ] -> [(Boolean)]";
    }
}
