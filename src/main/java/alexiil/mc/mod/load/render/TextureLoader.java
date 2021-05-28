package alexiil.mc.mod.load.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.opengl.KHRDebug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.SplashProgress;

import alexiil.mc.mod.load.CLSLog;
import alexiil.mc.mod.load.json.ResourceWrappingInputStream;

public final class TextureLoader {

    @Nullable
    public static InputStream loadTexture(ResourceLocation location) throws IOException {
        try {

            if ("config".equals(location.getResourceDomain())) {
                File fle = new File("config/customloadingscreen/" + location.getResourcePath());
                if (fle.exists()) {
                    return new FileInputStream(fle);
                }
            }

            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(location);
            return res != null ? new ResourceWrappingInputStream(res) : null;
        } catch (FileNotFoundException fnfe) {
            return null;
        }
    }

    public static void bindTexture(TextureManager manager, ResourceLocation location) {
        ITextureObject current = manager.getTexture(location);

        if (current != null) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, current.getGlTextureId());
            return;
        }

        load_from_config: if ("config".equals(location.getResourceDomain())) {

            File f = new File("config/customloadingscreen/" + location.getResourcePath());
            // File m = new File("config/customloadingscreen/" + location.getResourcePath() + ".mcmeta");

            if (!f.isFile()) {
                break load_from_config;
            }

            SimpleTexture texture = new SimpleTexture(location) {

                @Override
                public int getGlTextureId() {

                    if (this.glTextureId == -1) {
                        CLSLog.info("genGlTexture called on " + f);
                    }

                    return super.getGlTextureId();
                }

                @Override
                public void deleteGlTexture() {
                    super.deleteGlTexture();
                    if (this.glTextureId != -1) {
                        CLSLog.info("deleteGlTexture() called on " + f);
                    }
                }

                @Override
                public void loadTexture(IResourceManager resourceManager) throws IOException {
                    deleteGlTexture();
                    CLSLog.info("loadTexture() called on " + f);
                    try (FileInputStream is = new FileInputStream(f)) {

                        BufferedImage image = TextureUtil.readBufferedImage(is);
                        boolean blur = false;
                        boolean clamp = false;

                        // if (m.exists()) {
                        // try {
                        // TextureMetadataSection meta
                        // = (TextureMetadataSection) iresource.getMetadata("texture");
                        //
                        // if (meta != null) {
                        // blur = meta.getTextureBlur();
                        // clamp = meta.getTextureClamp();
                        // }
                        // } catch (RuntimeException runtimeexception) {
                        // LOGGER.warn("Failed reading metadata of: {}", this.textureLocation, runtimeexception);
                        // }
                        // }

                        synchronized (SplashProgress.class) {
                            TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), image, blur, clamp);
                        }

                        if (GLContext.getCapabilities().GL_KHR_debug) {
                            KHRDebug
                                .glObjectLabel(GL11.GL_TEXTURE, getGlTextureId(), "CLS_custom_tex_'" + location + "'");
                        }
                    }
                }
            };

            if (GLContext.getCapabilities().GL_KHR_debug) {
                KHRDebug.glPushDebugGroup(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, 10, "CLS_LoadCustomTexture");
            }
            manager.loadTexture(location, texture);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());

            if (GLContext.getCapabilities().GL_KHR_debug) {
                KHRDebug.glPopDebugGroup();
            }

            return;
        }

        if (GLContext.getCapabilities().GL_KHR_debug) {
            KHRDebug.glPushDebugGroup(KHRDebug.GL_DEBUG_SOURCE_APPLICATION, 10, "CLS_Render");
        }

        ITextureObject tex = manager.getTexture(location);
        manager.loadTexture(location, tex = new SimpleTexture(location));

        int id = tex.getGlTextureId();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        KHRDebug.glObjectLabel(GL11.GL_TEXTURE, id, "CLS_tex_'" + location + "'");

        if (GLContext.getCapabilities().GL_KHR_debug) {
            KHRDebug.glPopDebugGroup();
        }
    }
}
