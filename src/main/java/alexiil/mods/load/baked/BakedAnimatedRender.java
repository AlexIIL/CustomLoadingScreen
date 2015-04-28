package alexiil.mods.load.baked;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.baked.render.BakedImageRender;
import alexiil.mods.load.render.MinecraftDisplayerRenderer;
import alexiil.mods.load.render.RenderingStatus;

public class BakedAnimatedRender extends BakedImageRender {
    private final String resourceLocation;
    private final IBakedFunction<Double> frame;

    public BakedAnimatedRender(String resourceLocation, IBakedFunction<Double> x, IBakedFunction<Double> y, IBakedFunction<Double> width,
            IBakedFunction<Double> height, IBakedFunction<Double> uMin, IBakedFunction<Double> uMax, IBakedFunction<Double> vMin,
            IBakedFunction<Double> vMax, IBakedFunction<Double> frame) {
        super(resourceLocation, x, y, width, height, uMin, uMax, vMin, vMax);
        this.resourceLocation = resourceLocation;
        this.frame = frame;
    }

    @Override
    public void bindTexture(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        renderer.animator.bindTexture(resourceLocation, (int) (double) frame.call(status));
    }
}
