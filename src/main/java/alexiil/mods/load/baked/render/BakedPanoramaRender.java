package alexiil.mods.load.baked.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import alexiil.mods.load.baked.BakedRender;
import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.render.MinecraftDisplayerRenderer;
import alexiil.mods.load.render.RenderingStatus;

public class BakedPanoramaRender extends BakedRender {
    /** Timer used to rotate the panorama, increases every minecraft tick. (20tps) */
    private double actualAngle;
    private final BakedFunction<Double> angleFunc;
    private final ResourceLocation[] cubeSides;

    public BakedPanoramaRender(BakedFunction<Double> angle, String resourceLocation) {
        String[] strings = new String[6];
        for (int i = 0; i < 6; i++)
            strings[i] = resourceLocation.replace("_x", "_" + i);
        cubeSides = new ResourceLocation[6];
        for (int i = 0; i < 6; i++)
            cubeSides[i] = new ResourceLocation(strings[i]);
        angleFunc = angle;
    }

    /* This is mostly the same as GuiMainMenu.renderSkyBox() method, with a few things removed, and a bit of
     * customizability added. TODO: Add customizability */
    @Override
    public void render(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        actualAngle = angleFunc.call(status);
        drawPanorama();
    }

    private void drawPanorama() {
        Minecraft mc = Minecraft.getMinecraft();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
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
            GlStateManager.rotate(MathHelper.sin(((float) this.actualAngle) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
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

                mc.getTextureManager().bindTexture(cubeSides[l]);
                worldrenderer.startDrawingQuads();
                worldrenderer.setColorRGBA_I(16777215, 255 / (k + 1));
                float f4 = 0.0F;
                worldrenderer.addVertexWithUV(-1.0D, -1.0D, 1.0D, (double) (0.0F + f4), (double) (0.0F + f4));
                worldrenderer.addVertexWithUV(1.0D, -1.0D, 1.0D, (double) (1.0F - f4), (double) (0.0F + f4));
                worldrenderer.addVertexWithUV(1.0D, 1.0D, 1.0D, (double) (1.0F - f4), (double) (1.0F - f4));
                worldrenderer.addVertexWithUV(-1.0D, 1.0D, 1.0D, (double) (0.0F + f4), (double) (1.0F - f4));
                tessellator.draw();
                GlStateManager.popMatrix();
            }

            GlStateManager.popMatrix();
            GlStateManager.colorMask(true, true, true, false);
        }

        worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
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
