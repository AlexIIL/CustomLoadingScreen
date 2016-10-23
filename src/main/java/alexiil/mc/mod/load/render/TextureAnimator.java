package alexiil.mc.mod.load.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import alexiil.mc.mod.load.baked.BakedConfig;
import alexiil.mc.mod.load.baked.BakedRenderingPart;

public class TextureAnimator {
    public class AnimatedTexture {
        private final int[] ids;
        private final long[] lastUsed;
        private final BufferedImage[] images;
        private final int textureMilliSeconds;

        public AnimatedTexture(BufferedImage[] images, int totalPixels) {
            this.images = images;
            ids = new int[images.length];
            lastUsed = new long[images.length];
            Arrays.fill(ids, -1);
            if (totalPixels <= TEXTURE_PIXEL_MIN) {
                // If its a small animation, just upload all of them
                textureMilliSeconds = Integer.MAX_VALUE;
                for (int i = 0; i < images.length; i++)
                    bindFrame(i);
            } else if (totalPixels <= TEXTURE_PIXEL_CAP) textureMilliSeconds = TEXTURE_MILLI_SECONDS;
            else {
                // If this image is that big, expire frames quicker, relative to how big it actually is
                int higherPower = MathHelper.calculateLogBaseTwo(totalPixels / TEXTURE_PIXEL_CAP);
                textureMilliSeconds = TEXTURE_MILLI_SECONDS >> higherPower;
            }
        }

        public void uploadFramesAhead(int frame, int number) {
            for (int f = frame + 1; f < frame + number; f++) {
                int wf = f >= images.length ? f - images.length : f;
                if (wf >= images.length) break;// We have wrapped back around, twice (woops!)
                if (ids[wf] == -1) {
                    ids[wf] = TextureUtil.glGenTextures();
                    TextureUtil.uploadTextureImage(ids[wf], images[wf]);
                }
            }
        }

        public void bindFrame(int frame) {
            if (ids[frame] != -1) {
                GlStateManager.bindTexture(ids[frame]);
            } else {
                ids[frame] = TextureUtil.glGenTextures();
                TextureUtil.uploadTextureImage(ids[frame], images[frame]);
                GlStateManager.bindTexture(ids[frame]);
            }
            lastUsed[frame] = now;
            uploadFramesAhead(frame, TEXTURE_UPLOAD_AHEAD);
        }

        public void delete() {
            for (int i = 0; i < ids.length; i++)
                if (ids[i] != -1) deleteFrame(i);
        }

        /** This assumes that the frame is currently uploaded to the GPU in the first place ( (ids[frame] != -1) is
         * true) */
        private void deleteFrame(int frame) {
            TextureUtil.deleteTexture(ids[frame]);
            ids[frame] = -1;
        }

        private void tick() {
            for (int i = 0; i < ids.length; i++) {
                if (ids[i] != -1) {
                    if (lastUsed[i] + textureMilliSeconds < now) {
                        deleteFrame(i);
                    }
                }
            }
        }
    }

    /** How long to wait before deleting a texture. This is helpful mostly for textures with a greater number of frames
     * that you would like to be kept uploaded (say, part of an animated movie), and if they will only be shown once */
    private static final int TEXTURE_MILLI_SECONDS = 16384;// 2 ^ 14
    /** The cap before starting to delete older textures, in pixels. If the given texture has more total pixels than
     * this number, older textures will be deleted quicker. Default is a 256x texture at 60fps, for 10 seconds */
    private static final int TEXTURE_PIXEL_CAP = 256 * 256 * 60 * 10;
    /** The minimum number of pixels that are needed to actually use automatic texture deletion, and postpone uploading
     * until later. Default is a 32x image, at 20fps for 10 seconds. */
    private static final int TEXTURE_PIXEL_MIN = 32 * 32 * 20 * 10;
    /** The number of frames to upload ahead of time */
    private static final int TEXTURE_UPLOAD_AHEAD = 10;

    private Map<String, AnimatedTexture> animatedTextures = new HashMap<String, AnimatedTexture>();
    private long now = System.currentTimeMillis();

    public static boolean isAnimated(String resourceLocation) {
        ResourceLocation location = new ResourceLocation(resourceLocation);
        try {
            IResource res = Minecraft.getMinecraft().getResourceManager().getResource(location);
            final InputStream stream = res.getInputStream();
            for (ImageReader reader : ImmutableList.copyOf(ImageIO.getImageReaders(stream))) {
                try {
                    reader.setInput(stream);
                    boolean animated = reader.getNumImages(true) > 1;
                    reader.dispose();
                    return animated;
                } catch (IOException ignored) {} finally {
                    reader.dispose();
                }
            }
        } catch (IOException ignored) {}
        return false;
    }

    public TextureAnimator(BakedConfig images) {
        Minecraft mc = Minecraft.getMinecraft();
        for (BakedRenderingPart render : images.renderingParts) {
            String resource = render.render.getLocation();
            if (resource != null && isAnimated(resource)) {
                try {
                    IResource res = mc.getResourceManager().getResource(new ResourceLocation(resource));
                    final InputStream stream = res.getInputStream();
                    BufferedImage[] frames = null;
                    for (ImageReader reader : ImmutableList.copyOf(ImageIO.getImageReaders(stream))) {
                        try {
                            reader.setInput(stream);
                            int size = 0;
                            frames = new BufferedImage[reader.getNumImages(true)];
                            for (int i = 0; i < frames.length; i++) {
                                frames[i] = reader.read(i);
                                size += frames[i].getHeight() * frames[i].getWidth();
                            }
                            animatedTextures.put(resource, new AnimatedTexture(frames, size));
                            reader.dispose();
                            break;
                        } catch (IOException e) {
                            // TODO: do something about this!
                        } finally {
                            reader.dispose();
                        }
                    }
                } catch (IOException e) {
                    // TODO: do something about this!
                }
            }
        }
    }

    public void tick() {
        now = System.currentTimeMillis();
        for (AnimatedTexture tex : animatedTextures.values()) {
            tex.tick();
        }
    }

    public void close() {
        for (AnimatedTexture tex : animatedTextures.values()) {
            tex.delete();
        }
    }

    public void bindTexture(String resource, int frame) {
        if (animatedTextures.containsKey(resource)) {
            animatedTextures.get(resource).bindFrame(frame);
        }
    }
}
