package alexiil.mc.mod.load.render;

import static org.lwjgl.opengl.GL11.*;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

public class MainSplashRenderer {
    // These are all written to by the transformed ClsTransformer
    public static ResourceLocation fontLoc;
    public static volatile boolean done = false;
    public static volatile boolean pause = false;
    public static DummyTexture mojangLogoTex;

    private static FontRenderer fontRenderer;
    private static final Lock lock;
    private static final Semaphore mutex;

    private static final long start = System.currentTimeMillis();
    private static long diff;

    static {
        lock = get(SplashProgress.class, "lock");
        mutex = get(SplashProgress.class, "mutex");
        System.out.println("Got the concurrent fields");
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

    // This is called instead of SplashProgress$3.run
    public static void run() {
        fontRenderer = get(SplashProgress.class, "fontRenderer");
        System.out.println("run");

        while (!done) {
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
            if (diff < 2000) {
                renderMojangFrame();
            } else {
                renderFrame();
            }

            mutex.acquireUninterruptibly();
            Display.update();
            mutex.release();

            if (pause) {
                clearGL();
                setGL();
            }
            Display.sync(100);
        }
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

    // All references to this class are replaced with SplashProgress.Texture by ASM
    public static class DummyTexture {
        public void bind() {}

        public void texCoord(int i, float f, float f2) {}
    }

    private static void renderFrame() {

        // Actual drawing
        Iterator<ProgressBar> i = ProgressManager.barIterator();
        int y = 0;
        glColor3d(0, 0, 0);
        glPushMatrix();
        glScalef(2, 2, 1);
        glEnable(GL_TEXTURE_2D);

        String s = ((diff / 100L) / 10.0) + "s";
        fontRenderer.drawString(s, 0, -10, 0);

        while (i.hasNext()) {
            ProgressBar b = i.next();

            int startWidth = fontRenderer.getStringWidth(b.getTitle() + " ");

            fontRenderer.drawString(b.getTitle() + " ", -startWidth, y, 0);
            fontRenderer.drawString("- " + b.getMessage(), 0, y, 0);
            String bar = getProgress(b);
            fontRenderer.drawString(bar, -fontRenderer.getStringWidth(bar) / 2, y + 14, 0);

            y += 30;
        }

        glDisable(GL_TEXTURE_2D);
        glPopMatrix();
    }

    private static final int NUM_GAPS = 8;

    private static String getProgress(ProgressBar bar) {
        // Builds a string like [=====---] or [==>-----]
        String s = "[";
        double val = NUM_GAPS * bar.getStep() / (double) bar.getSteps();
        int count = (int) val;
        boolean endBig = val % 1 > 0.5;
        for (int i = 0; i < count; i++) {
            s += "=";
        }
        if (endBig & count < NUM_GAPS) {
            count++;
            s += ">";
        }
        for (int i = count; i < NUM_GAPS; i++) {
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
}
