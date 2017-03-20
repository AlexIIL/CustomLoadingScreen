package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

public class BakedConfig extends BakedTickable {
    public final BakedVariable[] variables;
    public final BakedRenderingPart[] renderingParts;
    public final BakedAction[] actions;
    public final BakedFactory[] factories;

    public BakedConfig(BakedVariable[] vars, BakedRenderingPart[] renderingParts, BakedAction[] actions, BakedFactory[] factories) {
        this.variables = vars;
        this.renderingParts = renderingParts;
        this.actions = actions;
        this.factories = factories;
    }

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        // NO-OP
        // This is handled specially by MinecraftDisplayerRenderer
    }
}
