package alexiil.mods.load.baked.func.stack;

import java.util.Deque;

import alexiil.mods.load.render.RenderingStatus;

public class BakedStackCastInteger extends BakedStackFunction {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        Object o = stack.pop();
        if (o instanceof Double) {
            stack.push((int) (double) (Double) o);
        }
        else if (o instanceof String) {
            stack.push(Integer.valueOf((String) o));
        }
        else
            throw new StackFunctionException("Tried to cast (" + o.getClass().getName() + ") to an integer!");
    }

    @Override
    public String toString() {
        return "Cast [(Double)] -> [(Integer)] OR [(String)] -> [(Integer)]";
    }

}
