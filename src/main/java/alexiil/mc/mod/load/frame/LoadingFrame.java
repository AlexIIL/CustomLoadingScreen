package alexiil.mc.mod.load.frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import alexiil.mc.mod.load.SingleProgressBarTracker;
import alexiil.mc.mod.load.SingleProgressBarTracker.LockUnlocker;

@SuppressWarnings("serial")
public class LoadingFrame extends JFrame {
    public static LoadingFrame openWindow() {
        String clsName = UIManager.getSystemLookAndFeelClassName();
        try {
            UIManager.setLookAndFeel(clsName);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        try {
            LoadingFrame frame = new LoadingFrame();
            frame.setBounds(getWindowBounds(frame));
            frame.setAlwaysOnTop(true);
            frame.setVisible(true);
            return frame;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Rectangle getWindowBounds(LoadingFrame frame) {
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle bounds = frame.getBounds();
        return new Rectangle((size.width - bounds.width) / 2, (size.height - bounds.height) / 2, bounds.width, bounds.height);
    }

    private final JProgressBar jprogress;

    /** Create the frame. */
    public LoadingFrame() {
        setTitle("Minecraft Loading");
        // setBounds(0, 0, 300, 100);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        jprogress = new JProgressBar(0, 100);
        contentPane.add(jprogress, BorderLayout.CENTER);

        pack();
        setSize(400, getHeight());
    }

    public void setProgress() {
        String status;
        String subStatus;
        int progress;
        boolean isInReload;
        try (LockUnlocker u = SingleProgressBarTracker.lockUpdate()) {
            status = SingleProgressBarTracker.getStatusText();
            subStatus = SingleProgressBarTracker.getSubStatus();
            progress = SingleProgressBarTracker.getProgress();
            isInReload = SingleProgressBarTracker.isInReload();
        }

        setTitle(isInReload ? status : (status + " - " + subStatus));
        double p = progress / SingleProgressBarTracker.MAX_PROGRESS_D;
        jprogress.setValue((int) (p * 100));
        jprogress.repaint();
    }
}
