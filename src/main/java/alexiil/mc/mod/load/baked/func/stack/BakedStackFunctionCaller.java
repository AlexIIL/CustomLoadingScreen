package alexiil.mc.mod.load.baked.func.stack;

import java.util.Deque;

import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedStackFunctionCaller extends BakedStackFunction {
    private final BakedFunction<?> func;

    public BakedStackFunctionCaller(BakedFunction<?> func) {
        this.func = func;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        try {
            int args = func.numArgs();
            Object[] objects = new Object[args];
            for (int i = args - 1; i >= 0; i--) {
                objects[i] = stack.pop();
            }
            stack.push(func.call(status, objects));
        }
        catch (FunctionException fe) {
            StackFunctionException sfe = new StackFunctionException("The function inside failed because {\n  " + fe.getMessage().replace("\n", "\n  ")
                + "}");
            sfe.initCause(fe);
            throw sfe;
        }
    }

    @Override
    public String toString() {
        return "Caller [] -> [(Any)]";
    }
}
