package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

import alexiil.mc.mod.load.expression.FunctionContext;
import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.InvalidExpressionException;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableDouble;

public class BakedPositionFunctional extends BakedInstruction {
    private final INodeDouble x, y, z;

    public static BakedPositionFunctional bake(String x, String y, FunctionContext functions) throws InvalidExpressionException {
        INodeDouble expX = GenericExpressionCompiler.compileExpressionDouble(x, functions).derive(null);
        INodeDouble expY = GenericExpressionCompiler.compileExpressionDouble(y, functions).derive(null);
        return new BakedPositionFunctional(expX, expY, new NodeImmutableDouble(0));
    }

    public BakedPositionFunctional(INodeDouble x, INodeDouble y, INodeDouble z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void render() {
        double x = this.x.evaluate();
        double y = this.y.evaluate();
        double z = this.z.evaluate();
        GL11.glTranslated(x, y, z);
    }
}
