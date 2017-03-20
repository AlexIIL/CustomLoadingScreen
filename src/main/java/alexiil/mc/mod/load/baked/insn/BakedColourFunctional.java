package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class BakedColourFunctional extends BakedInsn {
    private final INodeDouble a, r, g, b;

    public BakedColourFunctional(INodeDouble a, INodeDouble r, INodeDouble g, INodeDouble b) {
        this.a = a;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    @Override
    public void render() {
        GL11.glColor4d(r.evaluate(), g.evaluate(), b.evaluate(), a.evaluate());
    }
}
