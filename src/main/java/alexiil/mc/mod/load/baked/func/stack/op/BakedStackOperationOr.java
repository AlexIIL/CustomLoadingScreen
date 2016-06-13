package alexiil.mc.mod.load.baked.func.stack.op;

import java.util.Deque;

import alexiil.mc.mod.load.baked.func.stack.BakedStackFunction;
import alexiil.mc.mod.load.baked.func.stack.StackFunctionException;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedStackOperationOr extends BakedStackFunction {
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        boolean one = popBoolean(stack);
        boolean two = popBoolean(stack);
        stack.push(one || two);
    }

    @Override
    public String toString() {
        return "OR [ 1(Boolean) || 2(Boolean) ] -> [(Boolean)]";
    }
}
