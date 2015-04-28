package alexiil.mods.load.baked;

public class BakedConfig {
    public final BakedRenderingPart[] renderingParts;
    public final BakedAction[] actions;

    public BakedConfig(BakedRenderingPart[] renderingParts, BakedAction[] actions) {
        this.renderingParts = renderingParts;
        this.actions = actions;
    }
}
