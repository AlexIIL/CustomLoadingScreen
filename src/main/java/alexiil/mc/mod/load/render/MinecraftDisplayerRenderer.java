package alexiil.mc.mod.load.render;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glEnable;

import java.util.Map;

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
import alexiil.mc.mod.load.baked.BakedVariable;

public class MinecraftDisplayerRenderer {
    private static final ResourceLocation FONT_LOCATION = new ResourceLocation("textures/font/ascii.png");

    public final TextureAnimator animator;
    private final BakedVariable[] variables;
    private final BakedRenderingPart[] renderingParts;
    private final BakedAction[] actions;
    private final BakedFactory[] factories;
    private long lastTime;
    private Minecraft mc;
    private final Map<String, FontRenderer> fontRenderers = Maps.newHashMap();
    private final FontRendererSeparate _font_render_instance;
    public TextureManager textureManager;
    private boolean first = true;
    private SharedDrawable drawable;

    public MinecraftDisplayerRenderer(BakedConfig config, TextureAnimator animator) {
        this.animator = animator;
        mc = Minecraft.getMinecraft();

        textureManager = new TextureManager(mc.getResourceManager());
        _font_render_instance = new FontRendererSeparate(mc.gameSettings, FONT_LOCATION, textureManager, false);
        mc.refreshResources();
        textureManager.onResourceManagerReload(mc.getResourceManager());
        _font_render_instance.onResourceManagerReload(mc.getResourceManager());

        variables = config.variables;
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
        glEnable(GL_TEXTURE_2D);
        GlStateManager.enableTexture2D();

        GlStateManager.clearColor(1, 1, 1, 1);
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        GlStateManager.color(1, 1, 1, 1);

        for (BakedVariable variable : variables) {
            variable.tick(this);
        }

        for (BakedRenderingPart brp : renderingParts) {
            if (brp != null) {
                brp.tick(this);
            }
        }

        for (BakedFactory bf : factories) {
            bf.tick(this);
        }

        for (BakedAction ba : actions) {
            ba.tick(this);
        }

        // Post render stuffs
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 0);
        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.color(1, 1, 1, 1);
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
        _font_render_instance.destroy();
        drawable.destroy();
    }
}
