package alexiil.mc.mod.load.baked.factory;

import alexiil.mc.mod.load.baked.BakedFactory;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.BakedTickable;
import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.RenderingStatus;

public class FactoryElement extends BakedTickable {
    public final BakedFunction<Boolean> shouldDestroy;
    public final BakedRenderingPart component;

    public FactoryElement(BakedFactory factory) {
        this.shouldDestroy = factory.shouldDestroy;
        this.component = factory.component;
    }

    public void render(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        component.render(status, renderer);
    }

    public boolean shouldBeRemoved(RenderingStatus status) throws FunctionException {
        return shouldDestroy.call(status);
    }

    @Override
    public void tick(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        if (shouldBeRemoved(status))
            renderer.elements.remove(this);
        else
            render(status, renderer);
    }
}
