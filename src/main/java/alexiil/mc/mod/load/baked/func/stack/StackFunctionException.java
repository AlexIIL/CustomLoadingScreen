package alexiil.mc.mod.load.baked.func.stack;

import java.util.Deque;
import java.util.Map.Entry;

import alexiil.mc.mod.load.baked.func.BakedPostFixFunction;
import alexiil.mc.mod.load.render.RenderingStatus;

@SuppressWarnings("serial")
public class StackFunctionException extends Exception {
    public static String getMessage(BakedPostFixFunction<?> function, RenderingStatus status, Deque<?> currentStack, int currentPosition) {
        String[] functionList = function.getExecutionList(currentPosition);
        String lines = "Instructions:\n";
        for (String func : functionList)
            lines += func + "\n";
        lines += "Stack:\n";
        for (Object o : currentStack)
            lines += "  - " + o + " (" + o.getClass().getName() + ")" + "\n";
        if (status != null && status.tempVariables != null) {
            lines += "Variable Map:\n";
            for (Entry<String, Object> entry : status.tempVariables.entrySet())
                lines += "  - " + entry.getKey() + " = " + entry.getValue();
        }
        return lines;
    }

    public StackFunctionException(String message) {
        super(message);
    }
}
