package alexiil.mods.load.baked.func;

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
