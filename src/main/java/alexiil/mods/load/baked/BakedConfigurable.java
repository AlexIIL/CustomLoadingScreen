package alexiil.mods.load.baked;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.render.MinecraftDisplayerRenderer;
import alexiil.mods.load.render.RenderingStatus;

public abstract class BakedConfigurable {
    public abstract void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException;
}
