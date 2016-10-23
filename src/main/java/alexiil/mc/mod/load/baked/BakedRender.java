package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

public abstract class BakedRender extends BakedTickable {
    public abstract void render(MinecraftDisplayerRenderer renderer);

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        render(renderer);
    }

    public abstract String getLocation();
}
