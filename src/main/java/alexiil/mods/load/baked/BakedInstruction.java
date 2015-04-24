package alexiil.mods.load.baked;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.render.RenderingStatus;

public abstract class BakedInstruction {
    public abstract void render(RenderingStatus status) throws FunctionException;
}
