package alexiil.mc.mod.load.render;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.KHRDebug;

import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.SplashProgress;

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

        synchronized (SplashProgress.class) {
            TextureUtil.uploadTextureImageAllocate(getGlTextureId(), image, blur, clamp);
        }

        if (GLContext.getCapabilities().GL_KHR_debug) {
            KHRDebug.glObjectLabel(GL11.GL_TEXTURE, getGlTextureId(), "CLS_custom_tex_'" + location() + "'");
        }
    }
}
