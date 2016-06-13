package alexiil.mc.mod.load.baked.func.stack.op;

import java.util.Deque;

import alexiil.mc.mod.load.baked.func.stack.BakedStackFunction;
import alexiil.mc.mod.load.baked.func.stack.StackFunctionException;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedStackOperationAddition extends BakedStackFunction {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        Object one = popObject(stack);
        Object two = popObject(stack);
        if (one instanceof Double && two instanceof Double)
            stack.push((Double) one + (Double) two);
        else
            stack.push(two + "" + one);
    }

    @Override
    public String toString() {
        return "Addition [ 2(Double) + 1(Double) ] -> [(Double)] OR [ 2(Any) + 1(Any) ] -> [(String)]";
    }
}
