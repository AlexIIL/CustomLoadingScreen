package alexiil.mc.mod.load.baked.func.stack;

import java.util.Deque;
import java.util.Iterator;

import alexiil.mc.mod.load.baked.func.BakedPostFixFunction.IBakedStackFunction;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedStackArgument implements IBakedStackFunction {
    private final int argument;

    public BakedStackArgument(int arg) {
        if (arg < 0)
            throw new IllegalArgumentException("Cannot call argument number " + arg + ", as it was less than 0!");
        argument = arg;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        int i = 0;
        Object arg = null;
        boolean hasFound = false;
        Iterator iter = stack.descendingIterator();
        while (iter.hasNext()) {
            Object o = iter.next();
            if (i == argument) {
                arg = o;
                hasFound = true;
                break;
            }
            i++;
        }
        if (hasFound)
            stack.push(arg);
        else
            throw new StackFunctionException("Did not find argument #" + argument + "!");
    }

    @Override
    public String toString() {
        return "Argument #" + argument + " [] -> [(Any)]";
    }
}
