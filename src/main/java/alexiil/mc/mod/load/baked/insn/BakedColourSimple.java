package alexiil.mc.mod.load.baked.insn;

import net.minecraft.client.renderer.GlStateManager;

public class BakedColourSimple extends BakedInstruction {
    private final float r, g, b, a;

    public static BakedInstruction bakeColour() {
        return null;
    }

    public BakedColourSimple(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    @Override
    public void render() {
        GlStateManager.color(r, g, b, a);
    }
}
