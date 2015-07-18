package alexiil.mods.load.baked.func;

import alexiil.mods.load.baked.BakedConfigurable;
import alexiil.mods.load.render.RenderingStatus;

public abstract class BakedFunction<T> extends BakedConfigurable {
    public abstract T call(RenderingStatus status, Object... args) throws FunctionException;
    
    /** @return The number of arguments that should be passed into the function */
    public int numArgs() {
        return 0;
    }
}
