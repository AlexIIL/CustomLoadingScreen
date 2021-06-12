package alexiil.mc.mod.load.baked.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.TextureLoader;
import alexiil.mc.mod.load.render.TextureLoader.PreScannedImageData;

import buildcraft.lib.expression.node.value.NodeVariableDouble;

public class BakedImageRender extends BakedRenderPositioned {

    /** We only ever render 4 x (3 pos, 2 uv) ints each time then reset for the next face.
     * <p>
     * So this 64 is overkill. */
    private static final int TESS_INT_COUNT = 64;

    private final Tessellator tess = new Tessellator(TESS_INT_COUNT);

    protected final ResourceLocation res;
    private final BakedArea pos, tex;

    PreScannedImageData preScanned = null;

    public BakedImageRender(
        NodeVariableDouble varWidth, NodeVariableDouble varHeight, String res, BakedArea pos, BakedArea tex
    ) {
        super(varWidth, varHeight);
        this.res = new ResourceLocation(res);
        this.pos = pos;
        this.tex = tex;
    }

    @Override
    public void preLoad(MinecraftDisplayerRenderer renderer) {
        super.preLoad(renderer);

        preScanned = TextureLoader.preScan(res);
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
        BufferBuilder vb = tess.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(pos._x, pos._y + pos._h, 0).tex(tex._x, tex._y + tex._h).endVertex();
        vb.pos(pos._x + pos._w, pos._y + pos._h, 0).tex(tex._x + tex._w, tex._y + tex._h).endVertex();
        vb.pos(pos._x + pos._w, pos._y, 0).tex(tex._x + tex._w, tex._y).endVertex();
        vb.pos(pos._x, pos._y, 0).tex(tex._x, tex._y).endVertex();
        tess.draw();
    }

    public void bindTexture(MinecraftDisplayerRenderer renderer) {
        if (preScanned != null) {
            preScanned.bind(renderer.textureManager);
        } else {
            TextureLoader.bindTexture(renderer.textureManager, res);
        }
    }

    @Override
    public String getLocation() {
        return res.toString();
    }
}
