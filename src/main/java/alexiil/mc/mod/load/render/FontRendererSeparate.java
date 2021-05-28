package alexiil.mc.mod.load.render;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.CLSLog;
import alexiil.mc.mod.load.json.ConfigManager;

public class FontRendererSeparate extends FontRenderer {

    private static final BufferedImage EMPTY_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);

    private final Map<ResourceLocation, BufferedImage> textureData = new HashMap<>();
    private final Map<ResourceLocation, Integer> textureLocations = new HashMap<>();

    public FontRendererSeparate(
        GameSettings settings, ResourceLocation location, TextureManager textureManagerIn, boolean unicode
    ) {
        super(settings, location, textureManagerIn, unicode);

        loadTex(location);
        for (int i = 0; i < 256; i++) {
            if (i == 8) continue;
            if (0xd8 <= i && i <= 0xf8) continue;
            loadTex(new ResourceLocation(String.format("textures/font/unicode_page_%02x.png", i)));
        }
    }

    private BufferedImage loadTex(ResourceLocation location) {
        try (InputStream stream = ConfigManager.getInputStream(location)) {
            BufferedImage img = TextureUtil.readBufferedImage(stream);
            if (img == null) {
                CLSLog.warn("Failed to read a texture from " + location + " - " + stream);
                return EMPTY_IMAGE;
            }
            textureData.put(location, img);
            return img;
        } catch (FileNotFoundException e) {
            CLSLog.warn("loadTex(" + location + ") : " + e);
            return EMPTY_IMAGE;
        } catch (IOException e) {
            CLSLog.warn("loadTex(" + location + ") : " + e);
            return EMPTY_IMAGE;
        }
    }

    @Override
    protected void bindTexture(ResourceLocation location) {
        if (textureLocations == null) {
            // During init, so we don't care
            return;
        }
        Integer value = textureLocations.get(location);
        if (value == null) {
            BufferedImage img = textureData.computeIfAbsent(location, this::loadTex);
            if (img == null || img == EMPTY_IMAGE) {
                CLSLog.warn("Non-cached texture: '" + location + "'");
                return;
            }
            int next = GL11.glGenTextures();
            TextureUtil.uploadTextureImage(next, img);
            textureLocations.put(location, next);
            value = next;
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, value.intValue());
    }

    @Override
    protected IResource getResource(ResourceLocation location) throws IOException {
        if ("config".equals(location.getResourceDomain())) {
            InputStream stream = ConfigManager.getInputStream(location);
            InputStream metaStream = null;
            try {
                metaStream = ConfigManager.getInputStream(
                    new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".mcmeta")
                );
            } catch (IOException e) {
                // Ignored
            }
            return new SimpleResource(
                "cls config", location, stream, metaStream,
                Minecraft.getMinecraft().getResourcePackRepository().rprMetadataSerializer
            );
        }
        return super.getResource(location);
    }

    public void destroy() {
        for (Integer value : textureLocations.values()) {
            GL11.glDeleteTextures(value.intValue());
        }
        textureLocations.clear();
    }
}
