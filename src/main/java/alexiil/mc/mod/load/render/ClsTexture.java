package alexiil.mc.mod.load.render;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.KHRDebug;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class ClsTexture extends SimpleTexture {

    private BufferedImage image;
    private boolean blur;
    private boolean clamp;

    public ClsTexture(ResourceLocation location) {
        super(location);
    }

    public ResourceLocation location() {
        return textureLocation;
    }

    @Override
    public int getGlTextureId() {
        if (glTextureId == -1) {
            glTextureId = GL11.glGenTextures();
        }
        return glTextureId;
    }

    @Override
    public void deleteGlTexture() {
        if (glTextureId != -1) {
            GL11.glDeleteTextures(glTextureId);
            glTextureId = -1;
        }
    }

    public void loadImage(IResourceManager resourceManager) throws IOException {

        try (InputStream is = TextureLoader.openResourceStream(location())) {

            if (is == null) {
                throw new FileNotFoundException(location().toString());
            }

            image = TextureUtil.readBufferedImage(is);
            blur = false;
            clamp = false;

            // if (m.exists()) {
            // try {
            // TextureMetadataSection meta = (TextureMetadataSection) iresource.getMetadata("texture");
            //
            // if (meta != null) {
            // blur = meta.getTextureBlur();
            // clamp = meta.getTextureClamp();
            // }
            // } catch (RuntimeException runtimeexception) {
            // LOGGER.warn("Failed reading metadata of: {}", this.textureLocation, runtimeexception);
            // }
            // }
        }
    }

    @Override
    public void loadTexture(IResourceManager resourceManager) throws IOException {
        deleteGlTexture();

        if (image == null) {
            loadImage(resourceManager);
        }

        int id = getGlTextureId();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MIN_LOD, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LOD, 0);
        GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_LOD_BIAS, 0.0F);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, blur ? GL11.GL_LINEAR : GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, blur ? GL11.GL_LINEAR : GL11.GL_NEAREST);

        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, clamp ? GL11.GL_CLAMP : GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, clamp ? GL11.GL_CLAMP : GL11.GL_REPEAT);

        int width = image.getWidth();
        int height = image.getHeight();
        IntBuffer buffer = BufferUtils.createIntBuffer(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                buffer.put(image.getRGB(x, y));
            }
        }

        buffer.flip();

        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV,
            buffer
        );

        if (GLContext.getCapabilities().GL_KHR_debug) {
            KHRDebug.glObjectLabel(GL11.GL_TEXTURE, id, "CLS_custom_tex_'" + location() + "'");
        }
    }
}
