package alexiil.mods.load;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;

import org.apache.commons.lang3.StringUtils;

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
            // TODO Auto-generated method stub

        }

        @Override
        public void popProgress() {
            // TODO Auto-generated method stub

        }

        @Override
        public void addFuture(String text, double percent) {
            // TODO Auto-generated method stub

        }
    }

    public static class ConsoleDisplayer implements IDisplayer {
        private class ConsoleOutStream extends PrintStream {
            private final PrintStream out;

            public ConsoleOutStream(PrintStream out) {
                super(out);
                this.out = out;
            }

            @Override
            public void println(Object o) {
                out.println(o);
                printStatusLine();
            }

            @Override
            public void println(String text) {
                out.println(text);
                printStatusLine();

            }

            private synchronized void printStatusLine() {
                out.print(line);
            }
        }

        private static final String WHITESPACE = StringUtils.repeat(" ", 1);
        private static final int BAR_LENGTH = 20;
        private String line;

        @Override
        public void open(Configuration cfg) {
            System.setOut(new ConsoleOutStream(System.out));
            System.setErr(new ConsoleOutStream(System.err));
        }

        @Override
        public void updateProgress(String text, double percent) {
            printStatusLine(text, percent);
        }

        private void printStatusLine(String text, double percent) {
            String progress = "[";
            int length = (int) (BAR_LENGTH * percent);
            progress += StringUtils.repeat("=", length - 1);
            progress += ">";
            progress += StringUtils.repeat(" ", BAR_LENGTH - length);
            line = progress + "] " + (int) (percent * 100) + "% " + text + WHITESPACE + "\r";
            System.out.print(line);
        }

        @Override
        public void close() {}

        @Override
        public void pause() {}

        @Override
        public void resume() {}

        @Override
        public void pushProgress() {
            // TODO Auto-generated method stub

        }

        @Override
        public void popProgress() {
            // TODO Auto-generated method stub

        }

        @Override
        public void addFuture(String text, double percent) {
            // TODO Auto-generated method stub

        }
    }

    private static List<IDisplayer> displayers = new ArrayList<IDisplayer>();
    private static int clientState = -1;
    public static Configuration cfg;
    public static boolean playSound;
    public static File coreModLocation;
    public static ModContainer modContainer;

    public static boolean isClient() {
        if (clientState < 1000)
            return true;
        // TODO: fix this! its broken :/
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

        Configuration cfg = new Configuration(new File("./config/BetterLoadingScreen/config.cfg"));
        cfg.load();

        boolean useMinecraft = isClient();
        if (useMinecraft) {
            String comment =
                "Whether or not to use minecraft's display to show the progress. This looks better, but there is a possibilty of not being ";
            comment += "compatible, so if you do have any strange crash reports or compatability issues, try setting this to false";
            useMinecraft = cfg.getBoolean("useMinecraft", "general", true, comment);
        }

        String comment =
            "Whether or not to show a window seperate to minecraft to show the loading time -this can "
                + "be helpful if you set it to display above all windows if you alt tab while minecraft loads";
        boolean showFrame = cfg.getBoolean("showFrame", "general", false, comment);

        playSound = cfg.getBoolean("playSound", "general", true, "Play a sound after Minecraft has finished starting up");

        if (useMinecraft)
            displayers.add(new MinecraftDisplayerWrapper());
        if (showFrame)
            displayers.add(new FrameDisplayer());

        // if (System.console() != null)
        // displayers.add(new ConsoleDisplayer());
        // TODO: fix console logging!

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
        if (isClient() && playSound) {
            new Thread("BetterLoadingScreen|LoadedSound") {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException ignored) {}
                    MinecraftDisplayerWrapper.playFinishedSound();
                }
            }.start();;
        }
    }

    public static void minecraftDisplayFirstProgress() {
        displayProgress(Translation.translate("betterloadingscreen.state.minecraft_init", "Minecraft Initializing"), 0F);
    }

    public static void minecraftDisplayAfterForge() {
        displayProgress(Translation.translate("betterloadingscreen.state.minecraft_init", "Minecraft Initializing"), 0.55);
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
}
