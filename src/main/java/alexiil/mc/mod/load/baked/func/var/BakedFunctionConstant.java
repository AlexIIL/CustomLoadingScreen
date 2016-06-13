package alexiil.mc.mod.load.baked.func.var;

import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedFunctionConstant<T> extends BakedFunction<T> {
    private final T value;

    public BakedFunctionConstant(T value) {
        this.value = value;
    }

    @Override
    public T call(RenderingStatus status, Object... args) throws FunctionException {
        return value;
    }
}
