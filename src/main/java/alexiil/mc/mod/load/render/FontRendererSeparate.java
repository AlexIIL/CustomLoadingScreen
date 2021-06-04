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
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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

    private boolean __cls__replaced__underline;
    private boolean __cls__replaced__strikethrough;

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

    @Override
    protected void setColor(float r, float g, float b, float a) {
        GL11.glColor4f(r, g, b, a);
    }

    @Override
    protected void enableAlpha() {
        GL11.glEnable(GL11.GL_ALPHA);
    }

    @Override
    protected void doDraw(float width) {

        if (__cls__replaced__strikethrough || __cls__replaced__underline) {
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder bb = tess.getBuffer();
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            bb.begin(7, DefaultVertexFormats.POSITION);
            if (__cls__replaced__strikethrough) {
                int halfHeight = FONT_HEIGHT / 2;
                bb.pos(posX, posY + halfHeight, 0).endVertex();
                bb.pos(posX + width, posY + halfHeight, 0).endVertex();
                bb.pos(posX + width, posY + halfHeight - 1, 0).endVertex();
                bb.pos(posX, posY + halfHeight - 1, 0).endVertex();
            }
            if (__cls__replaced__underline) {
                bb.pos(posX - 1, posY + FONT_HEIGHT, 0).endVertex();
                bb.pos(posX + width, posY + FONT_HEIGHT, 0).endVertex();
                bb.pos(posX + width, posY + FONT_HEIGHT - 1, 0).endVertex();
                bb.pos(posX - 1, posY + FONT_HEIGHT - 1, 0).endVertex();
            }
            tess.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

        posX += width;
    }
}
