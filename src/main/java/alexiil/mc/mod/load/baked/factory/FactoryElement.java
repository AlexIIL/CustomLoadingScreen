package alexiil.mc.mod.load.baked.factory;

import alexiil.mc.mod.load.baked.BakedFactory;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.BakedTickable;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

public class FactoryElement extends BakedTickable {
    public final INodeBoolean shouldDestroy;
    public final BakedRenderingPart component;

    public FactoryElement(BakedFactory factory) {
        this.shouldDestroy = factory.shouldDestroy;
        this.component = factory.component;
    }

    public void render(MinecraftDisplayerRenderer renderer) {
        component.render(renderer);
    }

    public boolean shouldBeRemoved() {
        return shouldDestroy.evaluate();
    }

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        if (shouldBeRemoved()) renderer.elements.remove(this);
        else render(renderer);
    }
}
