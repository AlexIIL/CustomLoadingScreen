package alexiil.mc.mod.load.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.SplashProgress;

public final class TextureLoader {

    @Nullable
    public static InputStream loadTexture(ResourceLocation location) throws IOException {
        if ("config".equals(location.getResourceDomain())) {
            File fle = new File("config/customloadingscreen/" + location.getResourcePath());
            if (fle.exists()) {
                return new FileInputStream(fle);
            }
        }
        IResource res = Minecraft.getMinecraft().getResourceManager().getResource(location);
        return res != null ? res.getInputStream() : null;
    }

    public static void bindTexture(TextureManager manager, ResourceLocation location) {
        ITextureObject current = manager.getTexture(location);

        if (current != null) {
            manager.bindTexture(location);
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
                public void loadTexture(IResourceManager resourceManager) throws IOException {
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
                    }
                }
            };
            manager.loadTexture(location, texture);
        }
        manager.bindTexture(location);
    }
}
