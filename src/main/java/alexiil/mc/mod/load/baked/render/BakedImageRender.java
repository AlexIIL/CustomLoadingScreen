package alexiil.mc.mod.load.baked.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.node.value.NodeVariableDouble;

public class BakedImageRender extends BakedRenderPositioned {
    protected final ResourceLocation res;
    private final BakedArea pos, tex;

    public BakedImageRender(NodeVariableDouble varWidth, NodeVariableDouble varHeight, String res, BakedArea pos, BakedArea tex) {
        super(varWidth, varHeight);
        this.res = new ResourceLocation(res);
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
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder vb = tessellator.getBuffer();
        vb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
        vb.pos(pos._x, pos._y + pos._h, 0).tex(tex._x, tex._y + tex._h).endVertex();
        vb.pos(pos._x + pos._w, pos._y + pos._h, 0).tex(tex._x + tex._w, tex._y + tex._h).endVertex();
        vb.pos(pos._x + pos._w, pos._y, 0).tex(tex._x + tex._w, tex._y).endVertex();
        vb.pos(pos._x, pos._y, 0).tex(tex._x, tex._y).endVertex();
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
