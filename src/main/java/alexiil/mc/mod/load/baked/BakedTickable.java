package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.RenderingStatus;

public abstract class BakedTickable extends BakedConfigurable {
    public abstract void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException;
}
