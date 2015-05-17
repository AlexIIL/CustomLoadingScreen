package alexiil.mods.load.baked.func.stack;

import java.util.Deque;
import java.util.Map;

import com.google.common.collect.Maps;

import alexiil.mods.load.baked.func.BakedPostFixFunction.IBakedStackFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackArgument implements IBakedStackFunction {
    // Cache the stack objects as they
    private static final Map<Integer, BakedStackArgument> map = Maps.newHashMap();

    public static BakedStackArgument createBakedStackArgument(int arg) {
        if (arg < 0)
            throw new Error("Cannot call an argument less than 0!");
        if (map.containsKey(arg))
            return map.get(arg);

        BakedStackArgument bsa = new BakedStackArgument(arg);
        map.put(arg, bsa);
        return bsa;
    }

    private final int argument;

    private BakedStackArgument(int arg) {
        argument = arg;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        int i = 0;
        Object arg = null;
        boolean hasFound = false;
        for (Object o : stack) {
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
}
