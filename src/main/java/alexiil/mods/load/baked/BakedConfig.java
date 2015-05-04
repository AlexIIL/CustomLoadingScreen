package alexiil.mods.load.baked;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.render.MinecraftDisplayerRenderer;
import alexiil.mods.load.render.RenderingStatus;

public class BakedConfig extends BakedConfigurable {
    public final BakedRenderingPart[] renderingParts;
    public final BakedAction[] actions;
    public final BakedFactory[] factories;

    public BakedConfig(BakedRenderingPart[] renderingParts, BakedAction[] actions, BakedFactory[] factories) {
        this.renderingParts = renderingParts;
        this.actions = actions;
        this.factories = factories;
    }

    public void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        // NO-OP
        // This is handled specially by MinecraftDisplayerRenderer
    }
}
