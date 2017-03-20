package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

public class BakedRotationSimple extends BakedInsn {
    private final double angle, x, y, z;

    public BakedRotationSimple(double angle, double x, double y, double z) {
        this.angle = angle;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void render() {
        GL11.glRotated(angle, x, y, z);
    }
}
