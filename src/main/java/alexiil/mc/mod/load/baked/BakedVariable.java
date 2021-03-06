package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.NodeTypes;

public class BakedVariable extends BakedTickable {
    private final boolean constant;
    private final IVariableNode varNode;
    private final IExpressionNode expNode;

    public BakedVariable(boolean constant, IVariableNode varNode, IExpressionNode expNode) {
        this.constant = constant;
        this.varNode = varNode;
        this.expNode = expNode;
    }

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        if (!constant) {
            varNode.set(expNode);
        }
    }

    public BakedVariable copyAsConstant() {
        return new BakedVariable(true, varNode, NodeTypes.createConstantNode(expNode));
    }
}
