package alexiil.mc.mod.load.baked.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.node.value.NodeVariableLong;

public class BakedImageRender extends BakedRenderPositioned {
    protected final ResourceLocation res;
    private final BakedArea pos, tex;

    public BakedImageRender(NodeVariableLong varWidth, NodeVariableLong varHeight, String res, BakedArea pos, BakedArea tex) {
        super(varWidth, varHeight);
        this.res = new ResourceLocation(res);
        this.pos = pos;
        this.tex = tex;
    }

    @Override
    public void render(MinecraftDisplayerRenderer renderer) {
        double x = pos.x.evaluate();
        double y = pos.y.evaluate();
        double width = pos.width.evaluate();
        double height = pos.height.evaluate();
        double u = tex.x.evaluate();
        double v = tex.y.evaluate();
        double uWidth = tex.width.evaluate();
        double vHeight = tex.height.evaluate();
        varWidth.value = (long) width;
        varHeight.value = (long) height;
        bindTexture(renderer);
        drawRect(x, y, width, height, u, v, uWidth, vHeight);
    }

    public static void drawRect(double x, double y, double drawnWidth, double drawnHeight, double u, double v, double uWidth, double vHeight) {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vb = tessellator.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(x, y + drawnHeight, 0).tex(u, v + vHeight).endVertex();
        vb.pos(x + drawnWidth, y + drawnHeight, 0).tex(u + uWidth, v + vHeight).endVertex();
        vb.pos(x + drawnWidth, y, 0).tex(u + uWidth, v).endVertex();
        vb.pos(x, y, 0).tex(u, v).endVertex();
        tessellator.draw();
    }

    public void bindTexture(MinecraftDisplayerRenderer renderer) {
        renderer.textureManager.bindTexture(res);
    }

    @Override
    public String getLocation() {
        return res.toString();
    }
}
