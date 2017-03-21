package alexiil.mc.mod.load.baked.render;

import alexiil.mc.mod.load.baked.BakedRender;

import buildcraft.lib.expression.node.value.NodeVariableDouble;

public abstract class BakedRenderPositioned extends BakedRender {
    protected final NodeVariableDouble varWidth, varHeight;

    public BakedRenderPositioned(NodeVariableDouble varWidth, NodeVariableDouble varHeight) {
        this.varWidth = varWidth;
        this.varHeight = varHeight;
    }
}
