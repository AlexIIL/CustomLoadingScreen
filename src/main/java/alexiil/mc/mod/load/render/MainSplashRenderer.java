package alexiil.mc.mod.load.render;

import static org.lwjgl.opengl.GL11.GL_ALPHA_TEST;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_GREATER;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glAlphaFunc;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glColor3d;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glDepthFunc;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL11.glViewport;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

import alexiil.mc.mod.load.ClsManager;
import alexiil.mc.mod.load.CustomLoadingScreen;
import alexiil.mc.mod.load.progress.LongTermProgressTracker;
import alexiil.mc.mod.load.progress.SingleProgressBarTracker;
import alexiil.mc.mod.load.progress.SingleProgressBarTracker.LockUnlocker;

public class MainSplashRenderer {
    private static volatile boolean enableCustom = false;

    // These are all written to by the transformed ClsTransformer
    public static ResourceLocation fontLoc;
    public static volatile boolean pause = false;
    public static DummyTexture mojangLogoTex;

    private static FontRenderer fontRenderer;
    private static final Lock lock;
    private static final Semaphore mutex;

    private static long start;
    private static long diff;
    private static volatile boolean reachedConstruct = false;
    private static volatile boolean finishedLoading = false;

    static {
        lock = get(SplashProgress.class, "lock");
        mutex = get(SplashProgress.class, "mutex");
    }

    public static long getTotalTime() {
        return diff;
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(Class<?> cls, String name) {
        try {
            Field fld = cls.getDeclaredField(name);
            fld.setAccessible(true);
            return (T) fld.get(null);
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    public static void onReachConstruct() {
        if (!reachedConstruct) {
            try {
                enableCustom = ClsManager.load();
            } catch (Exception e) {
                e.printStackTrace();
            }
            reachedConstruct = true;
        }
    }

    // This is called by SplashProgress.finish
    public static void finish() {
        CustomLoadingScreen.finish();
        finishedLoading = true;
        lock.lock();
    }

    // This is called instead of SplashProgress$3.run
    public static void run() {
        fontRenderer = get(SplashProgress.class, "fontRenderer");

        boolean transitionOutDone = false;
        start = System.currentTimeMillis();

        while (!transitionOutDone) {
            glClearColor(1, 1, 1, 1);
            glClear(GL_COLOR_BUFFER_BIT);

            // matrix setup -- similar as SplashProgress
            int w = Display.getWidth();
            int h = Display.getHeight();
            glViewport(0, 0, w, h);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(-w / 2, w / 2, h / 2, -h / 2, -1, 1);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            diff = System.currentTimeMillis() - start;
            if (diff < 3000 || !reachedConstruct) {
                renderMojangFrame();
            } else if (!finishedLoading) {
                renderFrame();
            } else {
                transitionOutDone = renderTransitionFrame();
            }

            mutex.acquireUninterruptibly();
            Display.update();
            mutex.release();
            GL11.glFlush();

            if (finishedLoading && !reachedConstruct) {
                // We crashed
                break;
            }

            Display.sync(100);
            boolean grabUngrab = pause;// & !finishedLoading;
            if (grabUngrab) {
                clearGL();
            }
            if (grabUngrab) {
                setGL();
            }
        }
        LongTermProgressTracker.save(SingleProgressBarTracker.getProgressSections());
        clearGL();
    }

    private static void renderMojangFrame() {
        glColor4f(1, 1, 1, 1);
        glEnable(GL_TEXTURE_2D);
        mojangLogoTex.bind();
        glBegin(GL_QUADS);
        mojangLogoTex.texCoord(0, 0, 0);
        glVertex2f(-256, -256);
        mojangLogoTex.texCoord(0, 0, 1);
        glVertex2f(-256, 256);
        mojangLogoTex.texCoord(0, 1, 1);
        glVertex2f(256, 256);
        mojangLogoTex.texCoord(0, 1, 0);
        glVertex2f(256, -256);
        glEnd();
        glDisable(GL_TEXTURE_2D);
    }

    private static void renderFrame() {
        if (enableCustom) {
            ClsManager.renderFrame();
        } else {
            String status;
            String subStatus;
            double progress;
            try (LockUnlocker u = SingleProgressBarTracker.lockUpdate()) {
                status = SingleProgressBarTracker.getStatusText();
                subStatus = SingleProgressBarTracker.getSubStatus();
                progress = SingleProgressBarTracker.getProgress() / SingleProgressBarTracker.MAX_PROGRESS_D;
            }

            // Actual drawing
            int y = 0;
            glColor3d(0, 0, 0);
            glPushMatrix();
            glScalef(2, 2, 1);
            glEnable(GL_TEXTURE_2D);

            String s = ((diff / 100L) / 10.0) + "s";
            fontRenderer.drawString(s, 0, -10, 0);

            s = status + " - " + subStatus;
            fontRenderer.drawString(s, -fontRenderer.getStringWidth(s) / 2, -40, 0);
            String bar = getProgress(12, progress);
            fontRenderer.drawString(bar, -fontRenderer.getStringWidth(bar) / 2, -30, 0);

            Iterator<ProgressBar> i = ProgressManager.barIterator();
            while (i.hasNext()) {
                ProgressBar b = i.next();

                int startWidth = fontRenderer.getStringWidth(b.getTitle() + " ");

                fontRenderer.drawString(b.getTitle() + " ", -startWidth, y, 0);
                fontRenderer.drawString("- " + b.getMessage(), 0, y, 0);
                bar = getProgress(b);
                fontRenderer.drawString(bar, -fontRenderer.getStringWidth(bar) / 2, y + 14, 0);

                y += 30;
            }

            long max = Runtime.getRuntime().maxMemory();
            long total = Runtime.getRuntime().totalMemory();
            long free = Runtime.getRuntime().freeMemory();
            long used = total - free;

            String[] list = { //
                String.format("Mem: % 2d%% %03d/%03dMB", used * 100L / max, bytesToMb(used), bytesToMb(max)), //
                String.format("Allocated: % 2d%% %03dMB", total * 100L / max, bytesToMb(total)) //
            };

            int w = Display.getWidth();
            int h = Display.getHeight();

            int x = -w / 4;
            y = -h / 4;
            for (String s2 : list) {
                fontRenderer.drawString(s2, x, y, 0);
                y += 20;
            }

            glDisable(GL_TEXTURE_2D);
            glPopMatrix();
        }
    }

    private static long bytesToMb(long bytes) {
        return bytes / 1024L / 1024L;
    }

    private static boolean renderTransitionFrame() {
        if (enableCustom) {
            return ClsManager.renderTransitionFrame();
        } else {
            renderFrame();
            return true;
        }
    }

    private static String getProgress(ProgressBar bar) {
        return getProgress(8, bar.getStep() / (double) bar.getSteps());
    }

    private static String getProgress(int gaps, double perc) {
        // Builds a string like [=====---] or [==>-----]
        String s = "[";
        double val = gaps * perc;
        int count = (int) val;
        boolean endBig = val % 1 > 0.5;
        for (int i = 0; i < count; i++) {
            s += "=";
        }
        if (endBig & count < gaps) {
            count++;
            s += ">";
        }
        for (int i = count; i < gaps; i++) {
            s += "-";
        }
        return s + "]";
    }

    private static void setGL() {
        lock.lock();
        try {
            Display.getDrawable().makeCurrent();
        } catch (LWJGLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        glClearColor(1, 1, 1, 1);
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void clearGL() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayWidth = Display.getWidth();
        mc.displayHeight = Display.getHeight();
        mc.resize(mc.displayWidth, mc.displayHeight);
        glClearColor(1, 1, 1, 1);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);
        glEnable(GL_ALPHA_TEST);
        glAlphaFunc(GL_GREATER, .1f);
        try {
            Display.getDrawable().releaseContext();
        } catch (LWJGLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }

    // All references to this class are replaced with SplashProgress.Texture by ASM
    public static class DummyTexture {
        public void bind() {}

        public void texCoord(int i, float f, float f2) {}
    }
}
