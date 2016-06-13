package alexiil.mc.mod.load.baked.insn;

import alexiil.mc.mod.load.baked.BakedTickable;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.RenderingStatus;

public abstract class BakedInstruction extends BakedTickable {
    public abstract void render(RenderingStatus status) throws FunctionException;

    @Override
    public void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        render(status);
    }
}
