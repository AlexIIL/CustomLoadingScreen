package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.baked.factory.FactoryElement;
import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedFactory extends BakedTickable {
    public final BakedFunction<Boolean> shouldCreate, shouldDestroy;
    public final BakedRenderingPart component;

    public BakedFactory(BakedFunction<Boolean> shouldCreate, BakedFunction<Boolean> shouldDestroy, BakedRenderingPart component) {
        this.shouldCreate = shouldCreate;
        this.shouldDestroy = shouldDestroy;
        this.component = component;
    }

    /** @return A list with all of the elements that should be added */
    public FactoryElement getNewIfShould(RenderingStatus status) throws FunctionException {
        if (shouldCreate.call(status)) {
            return new FactoryElement(this);
        }
        return null;
    }

    @Override
    public void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        FactoryElement element = getNewIfShould(status);
        if (element != null)
            renderer.elements.add(element);
    }
}
