package alexiil.mc.mod.load.baked.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import net.minecraft.client.renderer.BufferBuilder;
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
    public void preLoad(MinecraftDisplayerRenderer renderer) {
        super.preLoad(renderer);

        for (ResourceLocation loc : cubeSides) {
            // TODO: Replace this with loading the texture data to bind on the correct thread.
            // TextureLoader.bindTexture(renderer.textureManager, loc);
        }
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

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        Project.gluPerspective(120.0F, 1.0F, 0.05F, 10.0F);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glDisable(GL11.GL_ALPHA);
        byte b0 = 8;

        for (int k = 0; k < b0 * b0; ++k) {
            GL11.glPushMatrix();
            float f1 = ((float) (k % b0) / (float) b0 - 0.5F) / 64.0F;
            float f2 = ((float) (k / b0) / (float) b0 - 0.5F) / 64.0F;
            float f3 = 0.0F;
            GL11.glTranslatef(f1, f2, f3);
            GL11.glRotatef(MathHelper.sin(((float) this.actualAngle) / 400.0F) * 25.0F + 20.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(-((float) this.actualAngle) * 0.1F, 0.0F, 1.0F, 0.0F);

            for (int l = 0; l < 6; ++l) {
                GL11.glPushMatrix();

                if (l == 1) {
                    GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 2) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 3) {
                    GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
                }

                if (l == 4) {
                    GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
                }

                if (l == 5) {
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
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
                GL11.glPopMatrix();
            }

            GL11.glPopMatrix();
            GL11.glColorMask(true, true, true, false);
        }

        vb.setTranslation(0.0D, 0.0D, 0.0D);
        GL11.glColorMask(true, true, true, true);
        GL11.glRotatef(-180.0F, 1.0F, 0.0F, 0.0F);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glEnable(GL11.GL_ALPHA);
    }

    @Override
    public String getLocation() {
        return null;
    }
}
