package alexiil.mods.load.baked.func;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import alexiil.mods.load.baked.func.stack.StackFunctionException;
import alexiil.mods.load.render.RenderingStatus;

public class BakedPostFixFunction<T> implements IBakedFunction<T> {
    public interface IBakedStackFunction {
        void doOperation(Deque<? extends Object> stack, RenderingStatus status) throws StackFunctionException;
    }

    private final List<IBakedStackFunction> toExecute;
    private final String function;

    /** @param executions
     *            The baked execution list
     * @param function
     *            The function that created the baked list. This is only used for debugging purposes if something goes
     *            wrong */
    public BakedPostFixFunction(List<IBakedStackFunction> executions, String function) {
        toExecute = executions;
        this.function = function;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T call(RenderingStatus status) throws FunctionException {
        Deque<Object> stack = new ArrayDeque<Object>();
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

    @Override
    public String toString() {
        return StringUtils.join(getExecutionList(-1), "\n");
    }
}
