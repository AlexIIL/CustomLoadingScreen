package alexiil.mods.load.baked;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.render.MinecraftDisplayerRenderer;
import alexiil.mods.load.render.RenderingStatus;

public abstract class BakedRender extends BakedTickable {
    public abstract void render(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException;

    public void populateVariableMap(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {}

    @Override
    public void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        render(status, renderer);
    }

    public abstract String getLocation();
}
