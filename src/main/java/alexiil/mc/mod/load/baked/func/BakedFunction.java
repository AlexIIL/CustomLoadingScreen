package alexiil.mc.mod.load.baked.func;

import alexiil.mc.mod.load.baked.BakedConfigurable;
import alexiil.mc.mod.load.render.RenderingStatus;

public abstract class BakedFunction<T> extends BakedConfigurable {
    public abstract T call(RenderingStatus status, Object... args) throws FunctionException;

    /** @return The number of arguments that should be passed into the function */
    public int numArgs() {
        return 0;
    }
}
