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
        int a = (int) ((value >> 24) & 0xFF);
        int r = (int) ((value >> 16) & 0xFF);
        int g = (int) ((value >> 8) & 0xFF);
        int b = (int) (value & 0xFF);
        GL11.glColor4d(r, g, b, a);
    }
}
