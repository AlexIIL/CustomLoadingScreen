package alexiil.mc.mod.load.baked.func.stack.op;

import java.util.Deque;

import alexiil.mc.mod.load.baked.func.stack.BakedStackFunction;
import alexiil.mc.mod.load.baked.func.stack.StackFunctionException;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedStackOperationEquality extends BakedStackFunction {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        Object one = stack.pop();
        Object two = stack.pop();
        stack.push(one.equals(two));
    }

    @Override
    public String toString() {
        return "Equality [ 1(Any) equals 2(Any) ] -> [(Boolean)]";
    }
}
