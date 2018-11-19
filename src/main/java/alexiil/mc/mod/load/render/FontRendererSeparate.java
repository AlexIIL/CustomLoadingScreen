package alexiil.mc.mod.load.render;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

public class FontRendererSeparate extends FontRenderer {

    private final Map<ResourceLocation, BufferedImage> textureData = new HashMap<>();
    private final Map<ResourceLocation, Integer> textureLocations = new HashMap<>();

    public FontRendererSeparate(GameSettings settings, ResourceLocation location, TextureManager textureManagerIn,
        boolean unicode) {
        super(settings, location, textureManagerIn, unicode);

        loadTex(location);
        // for (int i = 0; i < 256; i++) {

        // }
    }

    private void loadTex(ResourceLocation location) {
        try (IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location)) {
            textureData.put(location, TextureUtil.readBufferedImage(resource.getInputStream()));
        } catch (FileNotFoundException e) {
            throw new Error(e);
        } catch (IOException e) {
            throw new Error(e);
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
            BufferedImage img = textureData.get(location);
            if (img == null) {
                return;
            }
            int next = GL11.glGenTextures();
            TextureUtil.uploadTextureImage(next, img);
            textureLocations.put(location, next);
            value = next;
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, value.intValue());
    }

    public void destroy() {
        for (Integer value : textureLocations.values()) {
            GL11.glDeleteTextures(value.intValue());
        }
        textureLocations.clear();
    }
}
