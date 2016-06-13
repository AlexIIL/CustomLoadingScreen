package alexiil.mc.mod.load.baked.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedImageRender extends BakedRender {
    protected final ResourceLocation res;
    private final BakedFunction<Double> x, y, width, height, u, uWidth, v, vHeight;

    public BakedImageRender(String resourceLocation, BakedFunction<Double> x, BakedFunction<Double> y, BakedFunction<Double> width,
            BakedFunction<Double> height, BakedFunction<Double> uMin, BakedFunction<Double> uMax, BakedFunction<Double> vMin,
            BakedFunction<Double> vMax) {
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
    public void render(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        double x = this.x.call(status);
        double y = this.y.call(status);
        double width = this.width.call(status);
        double height = this.height.call(status);
        double u = this.u.call(status);
        double uWidth = this.uWidth.call(status);
        double v = this.v.call(status);
        double vHeight = this.vHeight.call(status);
        bindTexture(status, renderer);
        drawRect(x, y, width, height, u, v, uWidth, vHeight);
    }

    public void drawRect(double x, double y, double drawnWidth, double drawnHeight, double u, double v, double uWidth, double vHeight) {
        float f = 1 / 256F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        wr.pos(x, y + drawnHeight, 0).tex(u * f, (v + vHeight) * f).endVertex();;
        wr.pos(x + drawnWidth, y + drawnHeight, 0).tex((u + uWidth) * f, (v + vHeight) * f).endVertex();;
        wr.pos(x + drawnWidth, y, 0).tex((u + uWidth) * f, v * f).endVertex();;
        wr.pos(x, y, 0).tex(u * f, v * f).endVertex();
        tessellator.draw();
    }

    public void bindTexture(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        renderer.textureManager.bindTexture(res);
    }

    @Override
    public String getLocation() {
        return res.toString();
    }
}
