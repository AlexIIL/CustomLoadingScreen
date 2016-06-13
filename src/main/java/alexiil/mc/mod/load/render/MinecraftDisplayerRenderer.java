package alexiil.mc.mod.load.render;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.SharedDrawable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedAction;
import alexiil.mc.mod.load.baked.BakedConfig;
import alexiil.mc.mod.load.baked.BakedFactory;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.factory.FactoryElement;
import alexiil.mc.mod.load.baked.func.FunctionException;

public class MinecraftDisplayerRenderer {
    private final MinecraftDisplayer displayer;
    public final TextureAnimator animator;
    public final List<FactoryElement> elements = Lists.newArrayList();
    private final BakedRenderingPart[] renderingParts;
    private final BakedAction[] actions;
    private final BakedFactory[] factories;
    private long lastTime;
    private Minecraft mc;
    private final Map<String, FontRenderer> fontRenderers = Maps.newHashMap();
    public TextureManager textureManager;
    private boolean first = true;
    private SharedDrawable drawable;
    private final List<BakedRenderingPart> tempList = Lists.newArrayList();

    public MinecraftDisplayerRenderer(MinecraftDisplayer disp, BakedConfig config, TextureAnimator animator) {
        displayer = disp;
        this.animator = animator;
        mc = Minecraft.getMinecraft();
        textureManager = mc.renderEngine;
        renderingParts = config.renderingParts;
        actions = config.actions;
        factories = config.factories;

        lastTime = System.currentTimeMillis();
    }

    // Called 60 times per second to render the progress
    public void tick() throws FunctionException {
        long now = System.currentTimeMillis();
        try {
            ScaledResolution resolution = new ScaledResolution(mc);
            RenderingStatus status = displayer.status();
            status.tick(resolution, (now - lastTime) / 1000D);

            if (textureManager == null) {
                textureManager = mc.renderEngine = new TextureManager(mc.getResourceManager());
                mc.refreshResources();
                textureManager.onResourceManagerReload(mc.getResourceManager());
            }
            // if (textureManager != mc.renderEngine)
            // textureManager = mc.renderEngine;

            if (first) {
                try {
                    drawable = new SharedDrawable(Display.getDrawable());
                    drawable.makeCurrent();
                } catch (Throwable t) {
                    throw Throwables.propagate(t);
                }
                first = false;
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

            GlStateManager.clearColor(1, 1, 1, 1);
            GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            GlStateManager.color(1, 1, 1, 1);

            // Factory logic
            for (FactoryElement element : elements) {
                element.tick(status, this);
            }

            for (BakedFactory bf : factories) {
                bf.tick(status, this);
            }

            // Add all renders to the list
            tempList.clear();
            Collections.addAll(tempList, this.renderingParts);

            for (FactoryElement fe : elements) {
                tempList.add(fe.component);
            }

            // Actually render them
            for (BakedRenderingPart brp : tempList) {
                brp.tick(status, this);
            }

            // Post render stuffs
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 0);
            GlStateManager.enableAlpha();
            GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
            GlStateManager.color(1, 1, 1, 1);
            mc.updateDisplay();

            // Action stuffs
            for (BakedAction ba : actions) {
                ba.tick(status, this);
            }
        } catch (Throwable t) {
            if (t instanceof FunctionException) {
                lastTime = now;
                throw (FunctionException) t;
            }
            t.printStackTrace();
        } finally {
            lastTime = now;
        }
    }

    public FontRenderer fontRenderer(String fontTexture) {
        if (fontRenderers.containsKey(fontTexture)) return fontRenderers.get(fontTexture);
        FontRenderer font = new FontRenderer(mc.gameSettings, new ResourceLocation(fontTexture), textureManager, false);
        font.onResourceManagerReload(mc.getResourceManager());
        mc.refreshResources();
        font.onResourceManagerReload(mc.getResourceManager());
        fontRenderers.put(fontTexture, font);
        return font;
    }

    public void close() {
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.clearColor(1, 1, 1, 0);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT);
        drawable.destroy();
    }
}
