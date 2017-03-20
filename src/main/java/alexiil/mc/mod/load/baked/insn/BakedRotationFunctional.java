package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class BakedRotationFunctional extends BakedInsn {
    private final INodeDouble angle, x, y, z;

    public BakedRotationFunctional(INodeDouble angle, INodeDouble x, INodeDouble y, INodeDouble z) {
        this.angle = angle;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void render() {
        double angle = this.angle.evaluate();
        double x = this.x.evaluate();
        double y = this.y.evaluate();
        double z = this.z.evaluate();
        GL11.glRotated(angle, x, y, z);
    }
}
