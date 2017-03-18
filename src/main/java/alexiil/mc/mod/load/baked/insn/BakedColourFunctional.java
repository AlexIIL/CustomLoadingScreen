package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class BakedColourFunctional extends BakedInstruction {
    private final INodeDouble red, green, blue, alpha;

    public BakedColourFunctional(INodeDouble red, INodeDouble green, INodeDouble blue, INodeDouble alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public void render() {
        GL11.glColor4d(red.evaluate(), green.evaluate(), blue.evaluate(), alpha.evaluate());
    }
}
