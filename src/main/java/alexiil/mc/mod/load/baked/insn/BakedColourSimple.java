package alexiil.mc.mod.load.baked.insn;

import net.minecraft.client.renderer.GlStateManager;

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
        GlStateManager.color(r, g, b, a);
    }
}
