package alexiil.mc.mod.load.baked.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class BakedPanoramaRender extends BakedRender {

    /** We only ever render 4 x (3 pos, 2 uv, 1 colour) ints each time then reset for the next face.
     * <p>
     * So this 64 is overkill. */
    private static final int TESS_INT_COUNT = 0x40;

    private final Tessellator tess = new Tessellator(TESS_INT_COUNT);

    /** Timer used to rotate the panorama, increases every minecraft tick. (20tps) */
    private double actualAngle;
    private final INodeDouble angleFunc;
    private final ResourceLocation[] cubeSides;

    public BakedPanoramaRender(INodeDouble angle, String resourceLocation) {
        String[] strings = new String[6];
        for (int i = 0; i < 6; i++) {
            strings[i] = resourceLocation.replace("_x", "_" + i);
        }
        cubeSides = new ResourceLocation[6];
        for (int i = 0; i < 6; i++) {
            cubeSides[i] = new ResourceLocation(strings[i]);
        }
        angleFunc = angle;
    }

    @Override
    public void evaluateVariables(MinecraftDisplayerRenderer renderer) {}

    /* This is mostly the same as GuiMainMenu.renderSkyBox() method, with a few things removed, and a bit of
     * customizability added. TODO: Add customizability */
    @Override
    public void render(MinecraftDisplayerRenderer renderer) {
        actualAngle = angleFunc.evaluate();
        drawPanorama(renderer);
    }

    private void drawPanorama(MinecraftDisplayerRenderer renderer) {
        BufferBuilder vb = tess.getBuffer();

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.disableAlpha();
        byte b0 = 8;

        for (int k = 0; k < b0 * b0; ++k) {
            GlStateManager.pushMatrix();
            float f1 = ((float) (k % b0) / (float) b0 - 0.5F) / 64.0F;
            float f2 = ((float) (k / b0) / (float) b0 - 0.5F) / 64.0F;
            float f3 = 0.0F;
            GlStateManager.translate(f1, f2, f3);
            GlStateManager.rotate(MathHelper.sin(((float) this.actualAngle) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F,
                0.0F);
            GlStateManager.rotate(-((float) this.actualAngle) * 0.1F, 0.0F, 1.0F, 0.0F);

            for (int l = 0; l < 6; ++l) {
                GlStateManager.pushMatrix();

                if (l == 1) {
                    GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 2) {
                    GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 3) {
                    GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 4) {
                    GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (l == 5) {
                    GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
                }

                renderer.textureManager.bindTexture(cubeSides[l]);
                vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
                int rgb = 0xFF;
                int alpha = 255 / (k + 1);
                float f4 = 0.0F;
                vb.pos(-1.0D, -1.0D, 1.0D).tex(0.0F + f4, 0.0F + f4).color(rgb, rgb, rgb, alpha).endVertex();
                vb.pos(1.0D, -1.0D, 1.0D).tex(1.0F - f4, 0.0F + f4).color(rgb, rgb, rgb, alpha).endVertex();
                vb.pos(1.0D, 1.0D, 1.0D).tex(1.0F - f4, 1.0F - f4).color(rgb, rgb, rgb, alpha).endVertex();
                vb.pos(-1.0D, 1.0D, 1.0D).tex(0.0F + f4, 1.0F - f4).color(rgb, rgb, rgb, alpha).endVertex();
                tess.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        vb.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.colorMask(true, true, true, true);
        GlStateManager.rotate(-180.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.enableAlpha();
    }

    @Override
    public String getLocation() {
        return null;
    }
}
