package alexiil.mc.mod.load.baked.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class BakedImageRender extends BakedRender {
    protected final ResourceLocation res;
    private final INodeDouble x, y, width, height, u, uWidth, v, vHeight;

    public BakedImageRender(String resourceLocation, INodeDouble x, INodeDouble y, INodeDouble width, INodeDouble height, INodeDouble uMin, INodeDouble uMax, INodeDouble vMin, INodeDouble vMax) {
        res = new ResourceLocation(resourceLocation);
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.u = uMin;
        this.uWidth = uMax;
        this.v = vMin;
        this.vHeight = vMax;
    }

    @Override
    public void render(MinecraftDisplayerRenderer renderer) {
        double x = this.x.evaluate();
        double y = this.y.evaluate();
        double width = this.width.evaluate();
        double height = this.height.evaluate();
        double u = this.u.evaluate();
        double uWidth = this.uWidth.evaluate();
        double v = this.v.evaluate();
        double vHeight = this.vHeight.evaluate();
        bindTexture(renderer);
        drawRect(x, y, width, height, u, v, uWidth, vHeight);
    }

    public static void drawRect(double x, double y, double drawnWidth, double drawnHeight, double u, double v, double uWidth, double vHeight) {
        float f = 1 / 256F;
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vb = tessellator.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(x, y + drawnHeight, 0).tex(u * f, (v + vHeight) * f).endVertex();
        vb.pos(x + drawnWidth, y + drawnHeight, 0).tex((u + uWidth) * f, (v + vHeight) * f).endVertex();
        vb.pos(x + drawnWidth, y, 0).tex((u + uWidth) * f, v * f).endVertex();
        vb.pos(x, y, 0).tex(u * f, v * f).endVertex();
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
