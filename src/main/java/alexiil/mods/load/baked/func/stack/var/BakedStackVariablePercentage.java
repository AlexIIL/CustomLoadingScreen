package alexiil.mods.load.baked.func.stack.var;

import java.util.Deque;

import alexiil.mods.load.baked.func.stack.BakedStackFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackVariablePercentage extends BakedStackFunction {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) {
        stack.push(status.progressState.getCurrentProgress().percentage);
    }

    @Override
    public String toString() {
        return "Variable (percentage) [] -> [(Double)]";
    }
}
