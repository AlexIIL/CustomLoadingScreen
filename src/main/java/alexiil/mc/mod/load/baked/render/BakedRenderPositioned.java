package alexiil.mc.mod.load.baked.render;

import alexiil.mc.mod.load.baked.BakedRender;

import buildcraft.lib.expression.node.value.NodeVariableLong;

public abstract class BakedRenderPositioned extends BakedRender {
    protected final NodeVariableLong varWidth, varHeight;

    public BakedRenderPositioned(NodeVariableLong varWidth, NodeVariableLong varHeight) {
        this.varWidth = varWidth;
        this.varHeight = varHeight;
    }
}
