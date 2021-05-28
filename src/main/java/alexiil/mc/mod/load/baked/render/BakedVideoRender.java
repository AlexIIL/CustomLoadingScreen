package alexiil.mc.mod.load.baked.render;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.node.value.NodeVariableDouble;

public class BakedVideoRender extends BakedRenderPositioned {
    // TODO: Implement this!
    // Essentially as an alternative to using BakedAnimatedRender
    // (also so we don't have to make the frame-rate be passed in as a function)
    // I suppose we need to support that anyway though?

    public BakedVideoRender(NodeVariableDouble varWidth, NodeVariableDouble varHeight) {
        super(varWidth, varHeight);
    }

    @Override
    public void evaluateVariables(MinecraftDisplayerRenderer renderer) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public void render(MinecraftDisplayerRenderer renderer) {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }

    @Override
    public String getLocation() {
        // TODO Auto-generated method stub
        throw new AbstractMethodError("// TODO: Implement this!");
    }
}
