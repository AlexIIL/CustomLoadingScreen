package alexiil.mc.mod.load.baked.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.TextureLoader;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.node.value.NodeVariableDouble;

public class BakedSlideshowRender extends BakedRenderPositioned {

    /** We only ever render 4 x (3 pos, 2 uv) ints each time then reset for the next face.
     * <p>
     * So this 64 is overkill. */
    private static final int TESS_INT_COUNT = 0x40;

    private final Tessellator tess = new Tessellator(TESS_INT_COUNT);

    private final INodeLong colour;
    private final INodeDouble frame;
    protected final ResourceLocation[] res;
    private final BakedArea pos, tex;

    public BakedSlideshowRender(
        NodeVariableDouble varWidth, NodeVariableDouble varHeight, INodeLong colour, INodeDouble frame,
        ResourceLocation[] res, BakedArea pos, BakedArea tex
    ) {
        super(varWidth, varHeight);
        this.colour = colour;
        this.frame = frame;
        this.res = res;
        this.pos = pos;
        this.tex = tex;
    }

    @Override
    public void evaluateVariables(MinecraftDisplayerRenderer renderer) {
        pos.evaluate();
        tex.evaluate();
        varWidth.value = pos._w;
        varHeight.value = pos._h;
    }

    @Override
    public void render(MinecraftDisplayerRenderer renderer) {
        bindTexture(renderer);

        int c = (int) colour.evaluate();
        if ((c & 0xFF_00_00_00) == 0) {
            c |= 0xFF_00_00_00;
        } else if ((c & 0xFF_00_00_00) == 0x01_00_00_00) {
            c &= 0xFF_FF_FF;
        }

        if (c != -1) {
            GlStateManager.color(
                ((c >>> 16) & 0xFF) / 255f,
                ((c >>>  8) & 0xFF) / 255f,
                ((c >>>  0) & 0xFF) / 255f,
                ((c >>> 24) & 0xFF) / 255f
            );
        }

        BufferBuilder vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(pos._x, pos._y + pos._h, 0).tex(tex._x, tex._y + tex._h).endVertex();
        vb.pos(pos._x + pos._w, pos._y + pos._h, 0).tex(tex._x + tex._w, tex._y + tex._h).endVertex();
        vb.pos(pos._x + pos._w, pos._y, 0).tex(tex._x + tex._w, tex._y).endVertex();
        vb.pos(pos._x, pos._y, 0).tex(tex._x, tex._y).endVertex();
        tess.draw();

        if (c != -1) {
            GlStateManager.color(1, 1, 1, 1);
        }
    }

    public void bindTexture(MinecraftDisplayerRenderer renderer) {
        int index = (int) frame.evaluate();
        if (index < 0) {
            index = 0;
        } else {
            index %= res.length;
        }
        TextureLoader.bindTexture(renderer.textureManager, res[index]);
    }

    @Override
    public String getLocation() {
        return res.toString();
    }
}
