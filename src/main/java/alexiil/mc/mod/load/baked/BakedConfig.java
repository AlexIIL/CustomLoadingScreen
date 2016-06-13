package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedConfig extends BakedTickable {
    public final BakedRenderingPart[] renderingParts;
    public final BakedAction[] actions;
    public final BakedFactory[] factories;

    public BakedConfig(BakedRenderingPart[] renderingParts, BakedAction[] actions, BakedFactory[] factories) {
        this.renderingParts = renderingParts;
        this.actions = actions;
        this.factories = factories;
    }

    @Override
    public void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        // NO-OP
        // This is handled specially by MinecraftDisplayerRenderer
    }
}
