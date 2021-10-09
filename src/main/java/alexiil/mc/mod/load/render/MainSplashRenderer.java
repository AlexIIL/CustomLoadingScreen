package alexiil.mc.mod.load.render;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

import alexiil.mc.mod.load.CLSLog;
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

    private static final String SPLASH_ANIMATION_CLASS_NAME = "pl.asie.splashanimation.SplashAnimationRenderer";

    private static final boolean enableSplashAnimationCompat;
    private static final Field __SPLASH_ANIMATION_COMPAT__STAGE;
    private static final Method __SPLASH_ANIMATION_COMPAT__RUN;
    private static final Method __SPLASH_ANIMATION_COMPAT__FINISH;

    // Monitoring
    private static Thread mainThread = null;
    private static Thread monitorThread = null;

    /** A hint of how much time the main thread spends in the "blocked" state. Used to lower the time we spend in
     * Display.update() to ensure that the main thread isn't waiting for us for too long. */
    // *just* volatile is enough, as only 1 thread writes to this.
    private static volatile int blockedState = 0;
    private static volatile boolean inDisplayUpdate = false;

    static {
        lock = get(SplashProgress.class, "lock", Lock.class);
        mutex = get(SplashProgress.class, "mutex", Semaphore.class);

        Field stage = null;
        Method run = null;
        Method finish = null;
        try {
            Class<?> clazz = Class.forName(SPLASH_ANIMATION_CLASS_NAME);

            stage = clazz.getDeclaredField("stage");
            stage.setAccessible(true);

            run = clazz.getDeclaredMethod("run");
            run.setAccessible(true);

            finish = clazz.getDeclaredMethod("finish");
            finish.setAccessible(true);

        } catch (ClassNotFoundException cnfe) {
            CLSLog.info(
                "Not loading compat for SplashAnimation, as we didn't find '" + SPLASH_ANIMATION_CLASS_NAME + "'"
            );
        } catch (ReflectiveOperationException roe) {
            CLSLog.warn("Not loading compat for SplashAnimation, as some other error prevented us from using it:", roe);
        }

        __SPLASH_ANIMATION_COMPAT__STAGE = stage;
        __SPLASH_ANIMATION_COMPAT__RUN = run;
        __SPLASH_ANIMATION_COMPAT__FINISH = finish;
        enableSplashAnimationCompat = stage != null && run != null && finish != null;
    }

    public static long getTotalTime() {
        return diff;
    }

    private static <T> T get(Class<?> cls, String name, Class<T> type) {
        try {
            Field fld = cls.getDeclaredField(name);
            fld.setAccessible(true);
            return type.cast(fld.get(null));
        } catch (Throwable t) {
            throw new Error(t);
        }
    }

    /** @return The stage that SplashAnimation is at. the value 3 (or greater) indicate that it's finished rendering. */
    private static int getSplashAnimationStage() {
        if (!enableSplashAnimationCompat) {
            return 3;
        }
        try {
            return __SPLASH_ANIMATION_COMPAT__STAGE.getInt(null);
        } catch (IllegalArgumentException e) {
            return 3;
        } catch (IllegalAccessException e) {
            return 3;
        }
    }

    private static void invokeSplashAnimationRun() {
        if (enableSplashAnimationCompat) {
            try {
                __SPLASH_ANIMATION_COMPAT__RUN.invoke(null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                return;
            }
        }
    }

    private static void invokeSplashAnimationFinish() {
        if (enableSplashAnimationCompat) {
            try {
                __SPLASH_ANIMATION_COMPAT__FINISH.invoke(null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                return;
            }
        }
    }

    public static void onReachConstruct() {
        if (!reachedConstruct) {
            mainThread = Thread.currentThread();
            monitorThread = new Thread("CLS Monitor") {
                @Override
                public void run() {
                    Thread t;
                    while ((t = mainThread) != null) {
                        Thread.State state = t.getState();
                        if (state == State.BLOCKED && inDisplayUpdate) {
                            blockedState = (blockedState * 7000 + 3000 * 10_000) / 10_000;
                        } else {
                            blockedState = blockedState * 9900 / 10_000;
                        }

                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException ignored) {}
                    }
                }
            };
            monitorThread.setDaemon(true);
            monitorThread.start();
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
        mainThread = null;
        monitorThread = null;
    }

    // This is called instead of SplashProgress$3.run
    public static void run() {
        fontRenderer = get(SplashProgress.class, "fontRenderer", FontRenderer.class);

        boolean transitionOutDone = false;
        start = System.currentTimeMillis();

        while (!transitionOutDone) {
            if (CustomLoadingScreen.darkMode) {
                GL11.glClearColor(0, 0, 0, 1);
            } else {
                GL11.glClearColor(1, 1, 1, 1);
            }
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

            // matrix setup -- similar as SplashProgress
            int w = Display.getWidth();
            int h = Display.getHeight();
            GL11.glViewport(0, 0, w, h);
            GL11.glMatrixMode(GL11.GL_PROJECTION);
            GL11.glLoadIdentity();
            GL11.glOrtho(-w / 2, w / 2, h / 2, -h / 2, -1, 1);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
            GL11.glLoadIdentity();

            diff = System.currentTimeMillis() - start;
            if (diff < 2500 || !reachedConstruct) {
                renderMojangFrame();
            } else if (!finishedLoading) {
                int splashAnimationStage = getSplashAnimationStage();
                if (splashAnimationStage >= 3) {
                    renderFrame();
                } else {
                    invokeSplashAnimationRun();
                }
            } else {
                transitionOutDone = renderTransitionFrame();
            }

            // GL11.glPushMatrix();
            // GL11.glScalef(4, 4, 1);
            // GL11.glColor4f(1, 0, 1, 1);
            // GL11.glEnable(GL11.GL_TEXTURE_2D);

            // For the debug build always try for 100 fps
            int fpsLimit = CustomLoadingScreen.fpsLimit;
            // Reduce the fps to help prevent us from blocking the main thread.
            if (blockedState > 8_000) {
                fpsLimit = 6;
            } else if (blockedState > 4_000) {
                fpsLimit = 12;
            } else if (blockedState > 1_000) {
                fpsLimit = 24;
            }

            // String text = "" + blockedState / 1000 + " - max " + fpsLimit + "fps";
            // while (text.length() < 5) {
            // text = "0" + text;
            // }
            // fontRenderer.drawString(text, 0, 30, 0xFF_FF_FF_00);
            // glDisable(GL_TEXTURE_2D);
            // glPopMatrix();

            mutex.acquireUninterruptibly();
            inDisplayUpdate = true;
            Display.update();
            mutex.release();
            GL11.glFlush();
            inDisplayUpdate = false;

            if (finishedLoading && !reachedConstruct) {
                // We crashed
                break;
            }

            boolean grabUngrab = pause;// & !finishedLoading;
            if (grabUngrab) {
                clearGL();
            }
            Display.sync(fpsLimit);
            if (grabUngrab) {
                setGL();
            }
        }
        LongTermProgressTracker.save(SingleProgressBarTracker.getProgressSections());
        if (enableCustom) {
            ClsManager.finish();
        }
        clearGL();
        invokeSplashAnimationFinish();
    }

    private static void renderMojangFrame() {
        GL11.glColor4f(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        mojangLogoTex.bind();
        GL11.glBegin(GL11.GL_QUADS);
        mojangLogoTex.texCoord(0, 0, 0);
        GL11.glVertex2f(-256, -256);
        mojangLogoTex.texCoord(0, 0, 1);
        GL11.glVertex2f(-256, 256);
        mojangLogoTex.texCoord(0, 1, 1);
        GL11.glVertex2f(256, 256);
        mojangLogoTex.texCoord(0, 1, 0);
        GL11.glVertex2f(256, -256);
        GL11.glEnd();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
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
            GL11.glColor3d(0, 0, 0);
            GL11.glPushMatrix();
            GL11.glScalef(2, 2, 1);
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            int fontColour = CustomLoadingScreen.darkMode ? 0xFF_FF_FF_FF : 0;

            String s = ((diff / 100L) / 10.0) + "s";
            fontRenderer.drawString(s, 0, -10, fontColour);

            s = status + " - " + subStatus;
            fontRenderer.drawString(s, -fontRenderer.getStringWidth(s) / 2, -40, fontColour);
            String bar = getProgress(12, progress);
            fontRenderer.drawString(bar, -fontRenderer.getStringWidth(bar) / 2, -30, fontColour);

            Iterator<ProgressBar> i = ProgressManager.barIterator();
            while (i.hasNext()) {
                ProgressBar b = i.next();

                int startWidth = fontRenderer.getStringWidth(b.getTitle() + " ");

                fontRenderer.drawString(b.getTitle() + " ", -startWidth, y, fontColour);
                fontRenderer.drawString("- " + b.getMessage(), 0, y, fontColour);
                bar = getProgress(b);
                fontRenderer.drawString(bar, -fontRenderer.getStringWidth(bar) / 2, y + 14, fontColour);

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
                fontRenderer.drawString(s2, x, y, fontColour);
                y += 20;
            }

            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glPopMatrix();
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
        GL11.glClearColor(1, 1, 1, 1);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static void clearGL() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayWidth = Display.getWidth();
        mc.displayHeight = Display.getHeight();
        mc.resize(mc.displayWidth, mc.displayHeight);
        GL11.glClearColor(1, 1, 1, 1);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glAlphaFunc(GL11.GL_GREATER, .1f);
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
