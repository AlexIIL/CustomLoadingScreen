package alexiil.mods.load;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class LoadingFrame extends JFrame {
    private class ThreadIncrementer extends Thread {
        private final AtomicBoolean shouldIncrement = new AtomicBoolean(true);
        private final float from, to, diff;
        private final long time;
        private long timeLeft;

        public ThreadIncrementer(float from, float to, long timeLeft) {
            this.from = from;
            this.to = to;
            diff = to - from;
            this.timeLeft = timeLeft;
            this.time = timeLeft;
        }

        public void stopIncrementing() {
            shouldIncrement.set(false);
            incrementer = null;
        }

        @Override
        public void run() {
            while (timeLeft > 0 && shouldIncrement.get()) {
                try {
                    Thread.sleep(250);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                timeLeft -= 250;
                long timeDiff = time - timeLeft;
                double percent = timeDiff / (double) time;
                setProgress(from + percent * diff);
                repaint();
            }
            if (incrementer == this)
                incrementer = null;
        }
    }

    private JPanel contentPane;
    private JLabel lblState;
    private JProgressBar progressBar;
    private ThreadIncrementer incrementer;

    /** Launch the application. */
    public static LoadingFrame openWindow() {
        String clsName = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(clsName);
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            LoadingFrame frame = new LoadingFrame();
            frame.setBounds(getWindowBounds(frame));
            frame.setAlwaysOnTop(true);
            frame.setVisible(true);
            return frame;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Rectangle getWindowBounds(LoadingFrame frame) {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle bounds = frame.getBounds();
        return new Rectangle((size.width - bounds.width) / 2, (size.height - bounds.height) / 2, bounds.width, bounds.height);
    }

    /** Create the frame. */
    public LoadingFrame() {
        setTitle("Minecraft Loading");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 450, 85);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.NORTH);
        panel.setLayout(new BorderLayout(0, 0));

        lblState = new JLabel("State");
        panel.add(lblState, BorderLayout.NORTH);

        JPanel panel_1 = new JPanel();
        panel.add(panel_1, BorderLayout.CENTER);
        panel_1.setLayout(new BorderLayout(0, 0));

        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        panel_1.add(progressBar, BorderLayout.NORTH);
    }

    public void setMessage(String message) {
        lblState.setText(message);
    }

    public void setProgress(double percent) {
        progressBar.setValue((int) percent);
    }

    public void setProgressIncrementing(float from, float to, long howLongFor) {
        if (incrementer != null) {
            incrementer.stopIncrementing();
        }
        incrementer = new ThreadIncrementer(from, to, howLongFor);
        incrementer.start();
    }
}
