package alexiil.mc.mod.load.render;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.google.common.collect.ImmutableList;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import alexiil.mc.mod.load.CLSLog;
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
                for (int i = 0; i < images.length; i++) {
                    bindFrame(i, false);
                }
            } else if (totalPixels <= TEXTURE_PIXEL_CAP) textureMilliSeconds = TEXTURE_MILLI_SECONDS;
            else {
                // If this image is that big, expire frames quicker, relative to how big it actually is
                int higherPower = MathHelper.log2(totalPixels / TEXTURE_PIXEL_CAP);
                textureMilliSeconds = TEXTURE_MILLI_SECONDS >> higherPower;
            }
        }

        public void uploadFramesAhead(int frame, int number) {
            for (int f = frame + 1; f < frame + number; f++) {
                int wf = f >= images.length ? f - images.length : f;
                if (wf >= images.length) break;// We have wrapped back around, twice (woops!)
                if (ids[wf] == -1) {
                    ids[wf] = TextureUtil.glGenTextures();
                    uploadTextureImage(ids[wf], images[wf]);
                }
            }
        }

        public void bindFrame(int frame, boolean loop) {
            if (loop) {
                frame %= ids.length;
            } else {
                if (frame < 0) {
                    frame = 0;
                } else if (frame >= ids.length) {
                    frame = ids.length - 1;
                }
            }
            if (ids[frame] != -1) {
                GlStateManager.bindTexture(ids[frame]);
            } else {
                ids[frame] = TextureUtil.glGenTextures();
                uploadTextureImage(ids[frame], images[frame]);
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

    private static final int BUFFER_SIZE = 4 * 1024 * 1024;
    private static final IntBuffer DATA_BUFFER = GLAllocation.createDirectIntBuffer(BUFFER_SIZE);

    private Map<String, AnimatedTexture> animatedTextures = new HashMap<String, AnimatedTexture>();
    private long now = System.currentTimeMillis();

    public static boolean isAnimated(String resourceLocation) {
        ResourceLocation location = new ResourceLocation(resourceLocation);
        try {
            final InputStream stream = TextureLoader.loadTexture(location);
            if (stream == null) {
                return false;
            }
            try (MemoryCacheImageInputStream imageStream = new MemoryCacheImageInputStream(stream)) {
                for (ImageReader reader : ImmutableList.copyOf(ImageIO.getImageReaders(imageStream))) {
                    try {
                        reader.setInput(imageStream);
                        boolean animated = reader.getNumImages(true) > 1;
                        reader.dispose();
                        return animated;
                    } catch (IOException ignored) {} finally {
                        reader.dispose();
                    }
                }
            }
        } catch (IOException ignored) {}
        return false;
    }

    public TextureAnimator(BakedConfig images) {
        Minecraft mc = Minecraft.getMinecraft();
        for (BakedRenderingPart render : images.renderingParts) {
            String resource = render.render.getLocation();
            if (resource != null) {
                try {
                    final InputStream stream = TextureLoader.loadTexture(new ResourceLocation(resource));
                    if (stream == null) {
                        continue;
                    }
                    BufferedImage[] frames = null;
                    try (MemoryCacheImageInputStream imageStream = new MemoryCacheImageInputStream(stream)) {
                        for (ImageReader reader : ImmutableList.copyOf(ImageIO.getImageReaders(imageStream))) {
                            try {
                                reader.setInput(imageStream);
                                int size = 0;
                                frames = new BufferedImage[reader.getNumImages(true)];
                                if (frames.length < 2) {
                                    continue;
                                }
                                int read = 0;
                                for (int i = 0; i < frames.length; i++) {
                                    try {
                                        frames[read] = reader.read(i);
                                    } catch (IndexOutOfBoundsException e) {
                                        continue;
                                    }
                                    size += frames[read].getHeight() * frames[read].getWidth();
                                    read++;
                                }
                                if (read < frames.length) {
                                    frames = Arrays.copyOf(frames, read);
                                }
                                if (frames.length < 2) {
                                    continue;
                                }
                                CLSLog.info("Number of Frames = " + read);
                                animatedTextures.put(resource, new AnimatedTexture(frames, size));
                                reader.dispose();
                                break;
                            } catch (IOException e) {
                                // TODO: do something about this!
                            } finally {
                                reader.dispose();
                            }
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

    public void bindTexture(String resource, int frame, boolean loop) {
        if (animatedTextures.containsKey(resource)) {
            animatedTextures.get(resource).bindFrame(frame, loop);
        }
    }

    public static void uploadTextureImage(int textureId, BufferedImage texture) {
        TextureUtil.allocateTexture(textureId, texture.getWidth(), texture.getHeight());
        int width = texture.getWidth();
        int height = texture.getHeight();
        int count = BUFFER_SIZE / width;
        int[] data = new int[count * width];

        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, 10240, 9728);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, 10241, 9728);

        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, 10242, 10497);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, 10243, 10497);

        for (int i = 0; i < width * height; i += width * count) {
            int iteration = i / width;
            int thisHeight = Math.min(count, height - iteration);
            int size = width * thisHeight;
            texture.getRGB(0, iteration, width, thisHeight, data, 0, width);
            DATA_BUFFER.clear();
            DATA_BUFFER.put(data, 0, size);
            DATA_BUFFER.position(0).limit(size);
            GlStateManager.glTexSubImage2D(3553, 0, 0, iteration, width, thisHeight, 32993, 33639, DATA_BUFFER);
        }
        return;
    }

}
