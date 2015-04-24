package alexiil.mods.load.render;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.SharedDrawable;

import alexiil.mods.load.baked.BakedInstruction;
import alexiil.mods.load.baked.BakedRenderingPart;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.json.ConfigBase;

import com.google.common.base.Throwables;

public class MinecraftDisplayerRenderer {
    private final MinecraftDisplayer displayer;
    private final TextureAnimator animator;
    private final BakedRenderingPart[] renderingParts;
    private long lastTime;
    private Minecraft mc;
    private Map<String, FontRenderer> fontRenderers = new HashMap<String, FontRenderer>();
    private TextureManager textureManager;
    private boolean first = true;
    private SharedDrawable drawable;

    public MinecraftDisplayerRenderer(MinecraftDisplayer disp, ConfigBase config, TextureAnimator animator) {
        displayer = disp;
        this.animator = animator;
        mc = Minecraft.getMinecraft();
        textureManager = mc.renderEngine;
        lastTime = System.currentTimeMillis();
        renderingParts = config.bake();
    }

    // Called 60 times per second to render the progress
    public void tick() throws FunctionException {
        long now = System.currentTimeMillis();
        ScaledResolution resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        displayer.status().tick(displayer.getText(), displayer.getProgress(), resolution, (now - lastTime) / 1000D);

        if (textureManager == null) {
            textureManager = mc.renderEngine = new TextureManager(mc.getResourceManager());
            mc.refreshResources();
            textureManager.onResourceManagerReload(mc.getResourceManager());
        }
        if (textureManager != mc.renderEngine)
            textureManager = mc.renderEngine;

        if (first) {
            first = false;
            try {
                drawable = new SharedDrawable(Display.getDrawable());
                drawable.makeCurrent();
            }
            catch (Throwable t) {
                throw Throwables.propagate(t);
            }
        }

        // Pre render stuffs
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, (double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();

        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.clearColor(1, 1, 1, 1);

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

        GlStateManager.color(1, 1, 1, 1);

        // Actual rendering
        for (BakedRenderingPart brp : renderingParts) {
            if (brp.shouldRender.call(displayer.status())) {
                GlStateManager.pushMatrix();
                brp.render.populateVariableMap(displayer.status(), this);
                for (BakedInstruction insn : brp.instructions) {
                    insn.render(displayer.status());
                }
                brp.render.render(displayer.status(), this);
                GlStateManager.popMatrix();
            }
        }

        // Post render stuffs
        mc.updateDisplay();

        lastTime = now;
    }

    public FontRenderer fontRenderer(String fontTexture) {
        if (fontRenderers.containsKey(fontTexture))
            return fontRenderers.get(fontTexture);
        FontRenderer font = new FontRenderer(mc.gameSettings, new ResourceLocation(fontTexture), textureManager, false);
        font.onResourceManagerReload(mc.getResourceManager());
        mc.refreshResources();
        font.onResourceManagerReload(mc.getResourceManager());
        fontRenderers.put(fontTexture, font);
        return font;
    }

    public void close() {
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.color(1, 1, 1, 1);

        drawable.destroy();
    }
}
