package alexiil.mc.mod.load.render;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.SharedDrawable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.ClsManager;
import alexiil.mc.mod.load.ClsManager.Resolution;
import alexiil.mc.mod.load.baked.BakedAction;
import alexiil.mc.mod.load.baked.BakedConfig;
import alexiil.mc.mod.load.baked.BakedFactory;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.factory.FactoryElement;

public class MinecraftDisplayerRenderer {
    public final TextureAnimator animator;
    public final List<FactoryElement> elements = Lists.newArrayList();
    private final BakedRenderingPart[] renderingParts;
    private final BakedAction[] actions;
    private final BakedFactory[] factories;
    private long lastTime;
    private Minecraft mc;
    private final Map<String, FontRenderer> fontRenderers = Maps.newHashMap();
    private final FontRenderer _font_render_instance;
    public TextureManager textureManager;
    private boolean first = true;
    private SharedDrawable drawable;
    private final List<BakedRenderingPart> tempList = Lists.newArrayList();

    public MinecraftDisplayerRenderer(BakedConfig config, TextureAnimator animator) {
        this.animator = animator;
        mc = Minecraft.getMinecraft();

        textureManager = new TextureManager(mc.getResourceManager());
        _font_render_instance = new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), textureManager, false);
        mc.refreshResources();
        textureManager.onResourceManagerReload(mc.getResourceManager());
        _font_render_instance.onResourceManagerReload(mc.getResourceManager());

        renderingParts = config.renderingParts;
        actions = config.actions;
        factories = config.factories;

        lastTime = System.currentTimeMillis();
    }

    public void render() {
        Resolution resolution = ClsManager.RESOLUTION;

        // Pre render stuffs
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, resolution.getWidth(), resolution.getHeight(), 0.0D, -1, 1);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
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
            element.tick(this);
        }

        for (BakedFactory bf : factories) {
            bf.tick(this);
        }

        // Add all renders to the list
        tempList.clear();
        Collections.addAll(tempList, this.renderingParts);

        for (FactoryElement fe : elements) {
            tempList.add(fe.component);
        }

        // Actually render them
        for (BakedRenderingPart brp : tempList) {
            if (brp != null) {
                brp.tick(this);
            }
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
            ba.tick(this);
        }
    }

    public FontRenderer fontRenderer(String fontTexture) {
        // if (fontRenderers.containsKey(fontTexture)) {
        // return fontRenderers.get(fontTexture);
        // }
        // FontRenderer font = new FontRenderer(mc.gameSettings, new ResourceLocation(fontTexture), textureManager,
        // false);
        // // font.onResourceManagerReload(mc.getResourceManager());
        // // mc.refreshResources();
        // font.onResourceManagerReload(mc.getResourceManager());
        // fontRenderers.put(fontTexture, font);
        // return font;

        return _font_render_instance;
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
