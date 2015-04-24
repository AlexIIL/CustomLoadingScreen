package alexiil.mods.load.baked.func.stack.var;

import java.util.Deque;

import alexiil.mods.load.baked.func.stack.BakedStackFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackVariableScreenHeight extends BakedStackFunction {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) {
        stack.push((double) status.getScreenHeight());
    }

    @Override
    public String toString() {
        return "Variable (screenHeight) [] -> [(Double)]";
    }
}
