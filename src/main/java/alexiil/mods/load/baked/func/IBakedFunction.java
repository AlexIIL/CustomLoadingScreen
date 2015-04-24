package alexiil.mods.load.baked.func;

import alexiil.mods.load.render.RenderingStatus;

public interface IBakedFunction<T> {
    T call(RenderingStatus status) throws FunctionException;
}
