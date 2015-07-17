package alexiil.mods.load;

import java.io.File;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import com.google.common.collect.Queues;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;

import alexiil.mods.lib.ConfigAccess;
import alexiil.mods.load.render.MinecraftDisplayerWrapper;

public class ProgressDisplayer {
    public interface IDisplayer {
        void open(Configuration cfg);

        void updateProgress(String text, double percent);

        void addFuture(String text, double percent);

        void pushProgress();

        void popProgress();

        void close();

        void pause();

        void resume();
    }

    public static class FrameDisplayer implements IDisplayer {
        public LoadingFrame frame = null;

        @Override
        public void open(Configuration cfg) {
            frame = LoadingFrame.openWindow();
            if (frame != null) {
                frame.setMessage("Minecraft Forge Starting");
                frame.setProgress(0);
            }
        }

        @Override
        public void updateProgress(String text, double percent) {
            if (frame == null)
                return;
            frame.setMessage(text);
            frame.setProgress(percent * 100D);
            frame.repaint();
        }

        @Override
        public void close() {
            if (frame != null)
                frame.dispose();
        }

        @Override
        public void pause() {}

        @Override
        public void resume() {}

        @Override
        public void pushProgress() {
            if (frame != null)
                frame.pushProgress();
        }

        @Override
        public void popProgress() {
            if (frame != null)
                frame.popProgress();
        }

        @Override
        public void addFuture(String text, double percent) {}
    }

    private static List<IDisplayer> displayers = new ArrayList<IDisplayer>();
    private static int clientState = -1;
    public static Configuration cfg;
    // public static boolean playSound;
    public static File coreModLocation;
    public static ModContainer modContainer;

    public static boolean isClient() {
        if (clientState < 1000)
            return true;
        // FIXME: isClient() is broken!
        // TODO: Decide whether or not to drop server support.
        // It would make sense, right?
        // Because this isn't used on the server... right?
        if (clientState != -1)
            return clientState == 1;
        StackTraceElement[] steArr = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : steArr) {
            if (ste.getClassName().startsWith("net.minecraft.server")) {
                clientState = 0;
                return false;
            }
        }
        clientState = 1;
        return true;
    }

    public static void start(File coremodLocation) {
        coreModLocation = coremodLocation;
        if (coreModLocation == null)
            /* Assume this is a dev environment, and that the build dir is in bin, and the test dir has the same parent
             * as the bin dir... */
            coreModLocation = new File("./../bin/");
        ModMetadata md = new ModMetadata();
        md.name = Lib.Mod.NAME;
        md.modId = Lib.Mod.ID;
        modContainer = new DummyModContainer(md) {
            @Override
            public Class<?> getCustomResourcePackClass() {
                return FMLFileResourcePack.class;
            }

            @Override
            public File getSource() {
                return coreModLocation;
            }

            @Override
            public String getModId() {
                return Lib.Mod.ID;
            }
        };
        ConfigAccess ca = ConfigAccess.get(new File("./config/betterloadingscreen.cfg"), null);
        Configuration cfg = ca.cfg();

        boolean useMinecraft = isClient();
        if (useMinecraft) {
            String comment =
                "Whether or not to use minecraft's display to show the progress. This looks better, but there is a possibilty of not being ";
            comment += "compatible, so if you do have any strange crash reports or compatability issues, try setting this to false";
            useMinecraft = cfg.getBoolean("useMinecraft", "general", true, comment);
        }

        String comment =
            "Whether or not to show a window seperate to minecraft to show the loading time -this will automatically display above all windows, so you can see it even if you alt-tab to another window.";
        boolean showFrame = cfg.getBoolean("showFrame", "general", false, comment);

        // playSound = cfg.getBoolean("playSound", "general", true,
        // "Play a sound after Minecraft has finished starting up");

        if (useMinecraft)
            displayers.add(new MinecraftDisplayerWrapper());
        if (showFrame)
            displayers.add(new FrameDisplayer());

        for (IDisplayer displayer : displayers)
            displayer.open(cfg);
        cfg.save();
    }

    public static void displayProgress(String text, double percent) {
        for (IDisplayer displayer : displayers)
            displayer.updateProgress(text, percent);
    }

    public static void close() {
        for (IDisplayer displayer : displayers)
            displayer.close();
        displayers.clear();
    }

    public static void minecraftDisplayFirstProgress() {
        // displayProgress(Translation.translate("betterloadingscreen.state.minecraft_init", "Minecraft Initializing"),
        // 0F);
    }

    public static void minecraftDisplayAfterForge() {
        // displayProgress(Translation.translate("betterloadingscreen.state.minecraft_init", "Minecraft Initializing"),
        // 0.55);
    }

    public static void pause() {
        for (IDisplayer displayer : displayers)
            if (displayer != null)
                displayer.pause();
    }

    public static void resume() {
        for (IDisplayer displayer : displayers)
            if (displayer != null)
                displayer.resume();
    }

    public static void pushProgress() {
        for (IDisplayer displayer : displayers) {
            if (displayer != null)
                displayer.pushProgress();
        }
    }

    public static void popProgress() {
        for (IDisplayer displayer : displayers) {
            if (displayer != null)
                displayer.popProgress();
        }
    }

    private static Deque<Integer> stepsQueue = Queues.newArrayDeque();
    private static Deque<Integer> stepsCurrent = Queues.newArrayDeque();

    public static void forgeHook_ProgressManager_Push(String title, int steps, boolean timeEachStep) {
        pushProgress();
        stepsQueue.push(steps);
        stepsCurrent.push(0);
        displayProgress(title, 0);
    }

    public static void forgeHook_ProgressManager_Pop() {
        popProgress();
        stepsQueue.pop();
        stepsCurrent.pop();
    }

    public static void forgeHook_ProgressManager_ProgressBar_Step(String message) {
        int current = stepsCurrent.pop() + 1;
        stepsCurrent.push(current);
        double numSteps = (double) stepsQueue.peek();
        if (current > numSteps) {
            BLSLog.warn("Current (" + current + ") was greater than num steps(" + numSteps + ")!");
            current = (int) numSteps;
        }
        displayProgress(message, current / numSteps);
    }
}
