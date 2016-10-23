package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

public abstract class BakedTickable extends BakedConfigurable {
    public abstract void tick(MinecraftDisplayerRenderer renderer);
}
