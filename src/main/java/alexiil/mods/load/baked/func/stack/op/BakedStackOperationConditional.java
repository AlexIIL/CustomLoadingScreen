package alexiil.mods.load.baked.func.stack.op;

import java.util.Deque;

import alexiil.mods.load.baked.func.stack.BakedStackFunction;
import alexiil.mods.load.baked.func.stack.StackFunctionException;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackOperationConditional extends BakedStackFunction {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        Object two = stack.pop();
        Object one = stack.pop();
        Boolean test = popBoolean(stack);
        stack.push(test ? one : two);
    }

    @Override
    public String toString() {
        return "Conditional [ 3(Boolean) ? 1(Any) : 2(Any) ] -> [(Any)]";
    }
}
