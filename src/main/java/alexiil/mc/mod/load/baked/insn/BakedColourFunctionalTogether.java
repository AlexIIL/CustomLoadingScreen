package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

import buildcraft.lib.expression.api.IExpressionNode.INodeLong;

public class BakedColourFunctionalTogether extends BakedInsn {
    private final INodeLong argb;

    public BakedColourFunctionalTogether(INodeLong argb) {
        this.argb = argb;
    }

    @Override
    public void render() {
        long value = argb.evaluate();
        float a = ((value >> 24) & 0xFF) / 255f;
        float r = ((value >> 16) & 0xFF) / 255f;
        float g = ((value >>  8) & 0xFF) / 255f;
        float b = (value & 0xFF) / 255f;
        GL11.glColor4f(r, g, b, a);
    }
}
