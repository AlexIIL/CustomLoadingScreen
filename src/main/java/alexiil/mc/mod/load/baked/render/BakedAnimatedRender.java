package alexiil.mc.mod.load.baked.render;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeVariableDouble;

public class BakedAnimatedRender extends BakedImageRender {
    private final INodeDouble frame;

    public BakedAnimatedRender(NodeVariableDouble varWidth, NodeVariableDouble varHeight, String res, BakedArea pos, BakedArea tex, INodeDouble frame) {
        super(varWidth, varHeight, res, pos, tex);
        this.frame = frame;
    }

    @Override
    public void bindTexture(MinecraftDisplayerRenderer renderer) {
        renderer.animator.bindTexture(res.toString(), (int) frame.evaluate());
    }
}
