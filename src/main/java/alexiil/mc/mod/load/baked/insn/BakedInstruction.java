package alexiil.mc.mod.load.baked.insn;

import alexiil.mc.mod.load.baked.BakedTickable;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

public abstract class BakedInstruction extends BakedTickable {
    public abstract void render();

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        render();
    }
}
