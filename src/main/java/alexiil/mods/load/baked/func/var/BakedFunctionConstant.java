package alexiil.mods.load.baked.func.var;

import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.render.RenderingStatus;

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
