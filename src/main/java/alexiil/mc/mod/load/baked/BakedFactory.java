package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.baked.factory.FactoryElement;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

public class BakedFactory extends BakedTickable {
    public final INodeBoolean shouldCreate, shouldDestroy;
    public final BakedRenderingPart component;

    public BakedFactory(INodeBoolean shouldCreate, INodeBoolean shouldDestroy, BakedRenderingPart component) {
        this.shouldCreate = shouldCreate;
        this.shouldDestroy = shouldDestroy;
        this.component = component;
    }

    /** @return A list with all of the elements that should be added */
    public FactoryElement getNewIfShould() {
        if (shouldCreate.evaluate()) {
            return new FactoryElement(this);
        }
        return null;
    }

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        FactoryElement element = getNewIfShould();
        if (element != null) renderer.elements.add(element);
    }
}
