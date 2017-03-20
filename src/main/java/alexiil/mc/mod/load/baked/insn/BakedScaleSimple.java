package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

public class BakedScaleSimple extends BakedInsn {
    private final double x, y, z;

    public BakedScaleSimple(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void render() {
        GL11.glScaled(x, y, z);
    }
}
