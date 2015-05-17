package alexiil.mods.load.baked.func;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import alexiil.mods.load.baked.func.stack.StackFunctionException;
import alexiil.mods.load.render.RenderingStatus;

public class BakedPostFixFunction<T> extends BakedFunction<T> {
    public interface IBakedStackFunction {
        void doOperation(Deque<?> stack, RenderingStatus status) throws StackFunctionException;
    }

    private final List<IBakedStackFunction> toExecute;
    private final String function;
    private final int arguments;

    /** @param executions
     *            The baked execution list
     * @param function
     *            The function that created the baked list. This is only used for debugging purposes if something goes
     *            wrong */
    public BakedPostFixFunction(List<IBakedStackFunction> executions, String function, int arguments) {
        toExecute = executions;
        this.function = function;
        this.arguments = arguments;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T call(RenderingStatus status, Object... args) throws FunctionException {
        if (args == null && arguments != 0)
            throw new FunctionException(function, "Was not given any arguments! Wanted " + arguments + ", got 0!");
        if (args.length != arguments)
            throw new FunctionException(function, "Was not given enough Arguments! Wanted " + arguments + ", got " + args.length + "!");
        Deque<Object> stack = new ArrayDeque<Object>();
        if (args != null)
            for (Object o : args) {
                stack.push(o);
            }
        int i = 0;
        try {
            for (i = 0; i < toExecute.size(); i++) {
                IBakedStackFunction func = toExecute.get(i);
                func.doOperation(stack, status);
            }
            if (!stack.isEmpty()) {
                return (T) stack.pop();
            }
            throw new StackFunctionException("Empty stack at the end of the function");
        }
        catch (StackFunctionException e) {
            throw new FunctionException(function, e.getMessage() + "\n" + StackFunctionException.getMessage(this, status, stack, i));
        }
    }

    public String[] getExecutionList(int current) {
        String[] strings = new String[toExecute.size()];
        String start = "";
        if (strings.length > 10)
            start = " ";
        for (int i = 0; i < toExecute.size(); i++) {
            IBakedStackFunction func = toExecute.get(i);
            String s = i < 10 ? start : "";
            strings[i] = (current == i ? "->" : "   ") + i + s + ": " + (func == null ? "NULL" : func);
        }
        return strings;
    }

    public List<IBakedStackFunction> getExcuteList() {
        return Collections.unmodifiableList(toExecute);
    }

    @Override
    public String toString() {
        return StringUtils.join(getExecutionList(-1), "\n");
    }

    @Override
    public int numArgs() {
        return arguments;
    }
}
