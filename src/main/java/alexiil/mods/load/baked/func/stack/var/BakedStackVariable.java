package alexiil.mods.load.baked.func.stack.var;

import java.util.Deque;

import alexiil.mods.load.baked.func.stack.BakedStackFunction;
import alexiil.mods.load.baked.func.stack.StackFunctionException;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackVariable extends BakedStackFunction {

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        String name = popString(stack);
        boolean key = status.tempVariables.containsKey(name);
        if (key)
            stack.push(status.tempVariables.get(name));
        else
            throw new StackFunctionException("The specified variable (" + name + ") did not have an allocation on the map!");
    }

    @Override
    public String toString() {
        return "Variable [] -> [(Any)]";
    }
}
