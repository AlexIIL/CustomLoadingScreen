package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.NodeTypes;

public class BakedVariable extends BakedTickable {
    private final IVariableNode varNode;
    private final IExpressionNode expNode;

    public BakedVariable(IVariableNode varNode, IExpressionNode expNode) {
        this.varNode = varNode;
        this.expNode = expNode;
    }

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        varNode.set(expNode);
    }

    public BakedVariable copyAsConstant() {
        return new BakedVariable(varNode, NodeTypes.createConstantNode(expNode));
    }
}
