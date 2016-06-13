package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.RenderingStatus;

public abstract class BakedRender extends BakedTickable {
    public abstract void render(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException;

    public void populateVariableMap(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {}

    @Override
    public void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        render(status, renderer);
    }

    public abstract String getLocation();
}
