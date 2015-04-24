package alexiil.mods.load.baked.func.stack.var;

import java.util.Deque;

import alexiil.mods.load.baked.func.stack.BakedStackFunction;
import alexiil.mods.load.baked.func.stack.StackFunctionException;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackVariableStatus extends BakedStackFunction {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        String text = status.progressState.getCurrentProgress().status;
        stack.push(text == null ? "" : text);
    }

    @Override
    public String toString() {
        return "Variable (status) [] -> [(String)]";
    }
}
