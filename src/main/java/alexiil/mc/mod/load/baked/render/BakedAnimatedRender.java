package alexiil.mc.mod.load.baked.render;

import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

public class BakedAnimatedRender extends BakedImageRender {
    private final String resourceLocation;
    private final INodeDouble frame;

    public BakedAnimatedRender(String resourceLocation, INodeDouble x, INodeDouble y, INodeDouble width, INodeDouble height, INodeDouble uMin, INodeDouble uMax, INodeDouble vMin, INodeDouble vMax, INodeDouble frame) {
        super(resourceLocation, x, y, width, height, uMin, uMax, vMin, vMax);
        this.resourceLocation = resourceLocation;
        this.frame = frame;
    }

    @Override
    public void bindTexture(MinecraftDisplayerRenderer renderer) {
        renderer.animator.bindTexture(resourceLocation, (int) (double) frame.evaluate());
    }
}
