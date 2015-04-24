package alexiil.mods.load.baked;

import alexiil.mods.load.baked.func.IBakedFunction;

public class BakedRenderingPart {
    public final BakedInstruction[] instructions;
    public final BakedRender render;
    public final IBakedFunction<Boolean> shouldRender;

    public BakedRenderingPart(BakedInstruction[] instructions, BakedRender render, IBakedFunction<Boolean> shouldRender) {
        this.instructions = instructions;
        this.render = render;
        this.shouldRender = shouldRender;
    }
}
