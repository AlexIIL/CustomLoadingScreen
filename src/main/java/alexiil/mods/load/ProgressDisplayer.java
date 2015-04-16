package alexiil.mods.load;

import java.awt.GraphicsEnvironment;
import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProgressDisplayer {
    public interface IDisplayer {
        void open(Configuration cfg);

        void displayProgress(String text, double percent);

        void close();
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
        public void displayProgress(String text, double percent) {
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
    }

    public static class LoggingDisplayer implements IDisplayer {
        private Logger log;

        @Override
        public void open(Configuration cfg) {
            log = LogManager.getLogger("betterloadingscreen");
        }

        @Override
        public void displayProgress(String text, double percent) {
            log.info(text + " (" + (int) (percent * 100) + "%)");
        }

        @Override
        public void close() {}
    }

    private static IDisplayer displayer;
    private static int clientState = -1;
    public static Configuration cfg;
    public static boolean playSound;
    public static File coreModLocation;
    public static ModContainer modContainer;

    public static boolean isClient() {
        if (clientState != -1)
            return clientState == 1;
        StackTraceElement[] steArr = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : steArr) {
            if (ste.getClassName().startsWith("net.minecraftforge.fml.relauncher.ServerLaunchWrapper")) {
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
            coreModLocation = new File("./../bin/");
        // Assume this is a dev environment, and that the build dir is in bin, and the test dir has the same parent as
        // the bin dir...
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

        File fileOld = new File("./config/betterloadingscreen.cfg");
        File fileNew = new File("./config/BetterLoadingScreen/config.cfg");

        Configuration cfg;
        if (fileOld.exists())
            cfg = new Configuration(fileOld);
        else
            cfg = new Configuration(fileNew);

        boolean useMinecraft = isClient();
        if (useMinecraft) {
            String comment =
                    "Whether or not to use minecraft's display to show the progress. This looks better, but there is a possibilty of not being ";
            comment += "compatible, so if you do have nay strange crash reports or compatability issues, try setting this to false";
            useMinecraft = cfg.getBoolean("useMinecraft", "general", true, comment);
        }

        playSound = cfg.getBoolean("playSound", "general", true, "Play a sound after minecraft has finished starting up");

        if (useMinecraft)
            displayer = new MinecraftDisplayerWrapper();
        else if (!GraphicsEnvironment.isHeadless())
            displayer = new FrameDisplayer();
        else
            displayer = new LoggingDisplayer();
        displayer.open(cfg);
        cfg.save();
    }

    public static void displayProgress(String text, double percent) {
        displayer.displayProgress(text, percent);
    }

    public static void close() {
        if (displayer == null)
            return;
        displayer.close();
        displayer = null;
        if (isClient() && playSound) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e) {}
                    MinecraftDisplayerWrapper.playFinishedSound();
                }
            }.start();;
        }
    }

    public static void minecraftDisplayFirstProgress() {
        displayProgress(Translation.translate("betterloadingscreen.state.minecraft_init", "Minecraft Initializing"), 0F);
    }
}
