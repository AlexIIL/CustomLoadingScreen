package alexiil.mc.mod.load.frame;

import java.util.Timer;
import java.util.TimerTask;

public class FrameDisplayer {
    public final LoadingFrame frame;
    private Timer timer;

    public FrameDisplayer() {
        frame = LoadingFrame.openWindow();
    }

    public void start() {
        if (frame == null) {
            return;
        }
        Timer t = new Timer("CLS-frame-updater");
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                frame.setProgress();
            }
        };
        t.schedule(task, 0, 10);
    }

    public void finish() {
        if (timer != null) {
            timer.cancel();
        }
        if (frame != null) {
            frame.dispose();
        }
    }
}
