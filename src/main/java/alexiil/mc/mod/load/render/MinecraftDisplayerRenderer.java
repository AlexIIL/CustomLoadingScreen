package alexiil.mc.mod.load.render;

import java.util.Map;

import com.google.common.collect.Maps;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.KHRDebug;
import org.lwjgl.opengl.SharedDrawable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
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
    public TextureManagerCLS textureManager;
    private boolean first = true;
    private SharedDrawable drawable;

    public MinecraftDisplayerRenderer(BakedConfig config) {
        this.animator = new TextureAnimator(config);
        mc = Minecraft.getMinecraft();

        textureManager = new TextureManagerCLS(mc.getResourceManager());
        _font_render_instance = new FontRendererSeparate(mc.gameSettings, FONT_LOCATION, textureManager, false);
        mc.refreshResources();
        textureManager.onResourceManagerReload(mc.getResourceManager());
        _font_render_instance.onResourceManagerReload(mc.getResourceManager());

        variables = config.variables;
        renderingParts = config.renderingParts;
        actions = config.actions;
        factories = config.factories;

        lastTime = System.currentTimeMillis();

        config.preLoad(this);
    }

    public void render() {
        if (GLContext.getCapabilities().GL_KHR_debug) {
            KHRDebug.glPushDebugGroup(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, 10, "CLS_Render");
        }

        Resolution resolution = ClsManager.RESOLUTION;

        // Pre render stuffs
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, resolution.getWidth(), resolution.getHeight(), 0.0D, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_FOG);
        GL11.glDisable(GL11.GL_DEPTH);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glClearColor(1, 1, 1, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glEnable(GL11.GL_ALPHA);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        GL11.glColor4f(1, 1, 1, 1);

        animator.tick();

        for (BakedVariable variable : variables) {
            variable.tick(this);
        }

        for (BakedRenderingPart brp : renderingParts) {
            if (brp != null) {

                if (GLContext.getCapabilities().GL_KHR_debug) {
                    KHRDebug.glPushDebugGroup(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, 10, "" + brp.getOrigin());
                }

                brp.tick(this);

                if (GLContext.getCapabilities().GL_KHR_debug) {
                    KHRDebug.glPopDebugGroup();
                }
            }
        }

        for (BakedFactory bf : factories) {
            bf.tick(this);
        }

        for (BakedAction ba : actions) {
            ba.tick(this);
        }

        // Post render stuffs
        GL11.glEnable(GL11.GL_BLEND);
        GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 0);

        GL11.glEnable(GL11.GL_ALPHA);
        GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        GL11.glColor4f(1, 1, 1, 1);

        textureManager.onFrame();

        if (GLContext.getCapabilities().GL_KHR_debug) {
            KHRDebug.glPopDebugGroup();
        }
    }

    public FontRenderer fontRenderer(String fontTexture) {
        if ("missingno".equals(fontTexture)) {
            return _font_render_instance;
        }
        if (fontRenderers.containsKey(fontTexture)) {
            return fontRenderers.get(fontTexture);
        }
        FontRenderer font
            = new FontRendererSeparate(mc.gameSettings, new ResourceLocation(fontTexture), textureManager, false);
        // font.onResourceManagerReload(mc.getResourceManager());
        // mc.refreshResources();
        font.onResourceManagerReload(mc.getResourceManager());
        fontRenderers.put(fontTexture, font);
        return font;
    }

    public void close() {
        // GL11.glEnable(GL11.GL_BLEND);
        // GL14.glBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 0, 1);
        // GL11.glEnable(GL11.GL_ALPHA);
        // GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
        // GL11.glClearColor(1, 1, 1, 1);
        // GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        _font_render_instance.destroy();
        animator.close();
        textureManager.deleteAll();
    }
}
