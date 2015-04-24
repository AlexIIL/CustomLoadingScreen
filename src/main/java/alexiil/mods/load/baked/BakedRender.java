package alexiil.mods.load.baked;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.render.MinecraftDisplayerRenderer;
import alexiil.mods.load.render.RenderingStatus;

public abstract class BakedRender {
    public abstract void render(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException;

    public void populateVariableMap(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {}
}
