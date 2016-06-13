package alexiil.mc.mod.load.baked.render;

import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.RenderingStatus;

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
