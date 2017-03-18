package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class BakedScaleFunctional extends BakedInstruction {
    private final INodeDouble x, y, z;

    public BakedScaleFunctional(INodeDouble x, INodeDouble y, INodeDouble z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void render() {
        GL11.glScaled(x.evaluate(), y.evaluate(), z.evaluate());
    }
}
