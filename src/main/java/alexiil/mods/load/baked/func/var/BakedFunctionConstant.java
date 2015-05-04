package alexiil.mods.load.baked.func.var;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedFunctionConstant<T> implements IBakedFunction<T> {
    private final T value;

    public BakedFunctionConstant(T value) {
        this.value = value;
    }

    @Override
    public T call(RenderingStatus status) throws FunctionException {
        return value;
    }
}
