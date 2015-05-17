package alexiil.mods.load.baked.render;

import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.render.MinecraftDisplayerRenderer;
import alexiil.mods.load.render.RenderingStatus;

public class BakedAnimatedRender extends BakedImageRender {
    private final String resourceLocation;
    private final BakedFunction<Double> frame;

    public BakedAnimatedRender(String resourceLocation, BakedFunction<Double> x, BakedFunction<Double> y, BakedFunction<Double> width,
            BakedFunction<Double> height, BakedFunction<Double> uMin, BakedFunction<Double> uMax, BakedFunction<Double> vMin,
            BakedFunction<Double> vMax, BakedFunction<Double> frame) {
        super(resourceLocation, x, y, width, height, uMin, uMax, vMin, vMax);
        this.resourceLocation = resourceLocation;
        this.frame = frame;
    }

    @Override
    public void bindTexture(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        renderer.animator.bindTexture(resourceLocation, (int) (double) frame.call(status));
    }
}
