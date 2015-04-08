package alexiil.mods.load;

import java.awt.GraphicsEnvironment;
import java.io.File;

import net.minecraftforge.common.config.Configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProgressDisplayer {
    public interface IDisplayer {
        void open(Configuration cfg);

        void displayProgress(String text, float percent);

        void close();
    }

    public static class FrameDisplayer implements IDisplayer {
        private LoadingFrame frame = null;

        @Override
        public void open(Configuration cfg) {
            frame = LoadingFrame.openWindow();
            if (frame != null) {
                frame.setMessage("Minecraft Forge Starting");
                frame.setProgressIncrementing(0, 20, 4000);
            }
        }

        @Override
        public void displayProgress(String text, float percent) {
            if (frame == null)
                return;
            frame.setMessage(text);
            frame.setProgress(percent * 100F);
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
        public void displayProgress(String text, float percent) {
            log.info(text + " (" + (int) (percent * 100) + "%)");
        }

        @Override
        public void close() {}
    }

    private static IDisplayer displayer;

    public static void start() {
        String comment = "Whether or not to use minecraft's display to show the progress. This looks better, but there is a possibilty of not being ";
        comment += "compatible, so if you do have nay strange crash reports or compatability issues, try setting this to false";
        Configuration cfg = new Configuration(new File("./config/betterloadingscreen.cfg"));
        boolean useMinecraft = cfg.getBoolean("useMinecraft", "general", true, comment);
        if (useMinecraft)
            displayer = new MinecraftDisplayerWrapper();
        else if (!GraphicsEnvironment.isHeadless())
            displayer = new FrameDisplayer();
        else
            displayer = new LoggingDisplayer();
        displayer.open(cfg);
        cfg.save();
    }

    public static void displayProgress(String text, float percent) {
        displayer.displayProgress(text, percent);
    }

    public static void close() {
        if (displayer == null)
            return;
        displayer.close();
        displayer = null;
    }
}
