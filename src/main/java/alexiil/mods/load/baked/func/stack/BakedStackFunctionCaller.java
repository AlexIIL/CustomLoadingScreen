package alexiil.mods.load.baked.func.stack;

import java.util.Deque;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackFunctionCaller extends BakedStackFunction {
    private final IBakedFunction<?> func;

    public BakedStackFunctionCaller(IBakedFunction<?> func) {
        this.func = func;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) throws StackFunctionException {
        try {
            stack.push(func.call(status));
        }
        catch (FunctionException fe) {
            StackFunctionException sfe =
                new StackFunctionException("the function inside failed because {\n  " + fe.getMessage().replace("\n", "\n  ") + "}");
            sfe.initCause(fe);
            throw sfe;
        }
    }

    @Override
    public String toString() {
        return "Caller [] -> [(Any)]";
    }
}
