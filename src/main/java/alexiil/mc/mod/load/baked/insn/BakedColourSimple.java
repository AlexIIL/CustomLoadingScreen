package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

public class BakedColourSimple extends BakedInsn {
    private final float a, r, g, b;

    public BakedColourSimple(float a, float r, float g, float b) {
        this.a = a;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public void render() {
        GL11.glColor4f(r, g, b, a);
    }
}
