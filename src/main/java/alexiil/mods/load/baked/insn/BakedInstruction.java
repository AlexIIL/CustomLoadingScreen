package alexiil.mods.load.baked.insn;

import alexiil.mods.load.baked.BakedTickable;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.render.MinecraftDisplayerRenderer;
import alexiil.mods.load.render.RenderingStatus;

public abstract class BakedInstruction extends BakedTickable {
    public abstract void render(RenderingStatus status) throws FunctionException;

    @Override
    public void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        render(status);
    }
}
