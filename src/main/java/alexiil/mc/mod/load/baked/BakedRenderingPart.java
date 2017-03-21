package alexiil.mc.mod.load.baked;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.GlStateManager;

import alexiil.mc.mod.load.baked.insn.BakedInsn;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public class BakedRenderingPart extends BakedTickable {
    public final BakedInsn[] instructions;
    public final BakedRender render;
    public final INodeBoolean shouldRender;

    public BakedRenderingPart(BakedInsn[] instructions, BakedRender render, INodeBoolean shouldRender) {
        this.instructions = instructions;
        this.render = render;
        if (render == null) throw new NullPointerException("render");
        this.shouldRender = shouldRender;
    }

    public void render(MinecraftDisplayerRenderer renderer) {
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        render.evaluateVariables(renderer);
        for (BakedInsn insn : instructions) {
            insn.render();
        }
        render.render(renderer);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        if (shouldRender.evaluate()) {
            render(renderer);
        }
    }
}
