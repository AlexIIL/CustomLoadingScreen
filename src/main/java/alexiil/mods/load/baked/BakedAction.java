package alexiil.mods.load.baked;

import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.render.MinecraftDisplayerRenderer;
import alexiil.mods.load.render.RenderingStatus;

public abstract class BakedAction extends BakedTickable {
    protected final BakedFunction<Boolean> conditionStart, conditionEnd;
    private boolean started = false;

    public BakedAction(BakedFunction<Boolean> conditionStart, BakedFunction<Boolean> conditionEnd) {
        this.conditionStart = conditionStart;
        this.conditionEnd = conditionEnd;
    }

    @Override
    public void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        if (!started && conditionStart.call(status)) {
            started = true;
            start(status);
        }
        else if (started) {
            tick(status);
        }
        if (started && conditionEnd.call(status)) {
            started = false;
            end(status);
        }
    }

    /** Called the first time conditionStart resolves to true */
    protected abstract void start(RenderingStatus status) throws FunctionException;

    /** Called once per tick after start() has been called, and before stop() has been called */
    protected abstract void tick(RenderingStatus status) throws FunctionException;

    /** Called whenever conditionEnd resolves to true, provided that start() has already been called */
    protected abstract void end(RenderingStatus status) throws FunctionException;
}
