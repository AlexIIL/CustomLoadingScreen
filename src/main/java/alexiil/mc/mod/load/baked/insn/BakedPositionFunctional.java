package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class BakedPositionFunctional extends BakedInstruction {
    private final INodeDouble x, y, z;

    public static BakedPositionFunctional bake(String x, String y, FunctionContext functions) throws InvalidExpressionException {
        INodeDouble expX = GenericExpressionCompiler.compileExpressionDouble(x, functions);
        INodeDouble expY = GenericExpressionCompiler.compileExpressionDouble(y, functions);
        return new BakedPositionFunctional(expX, expY, NodeConstantDouble.ZERO);
    }

    public BakedPositionFunctional(INodeDouble x, INodeDouble y, INodeDouble z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void render() {
        GL11.glTranslated(x.evaluate(), y.evaluate(), z.evaluate());
    }
}
