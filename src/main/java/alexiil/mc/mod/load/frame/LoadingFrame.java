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

    private final JProgressBar progress;

    /** Create the frame. */
    public LoadingFrame() {
        setTitle("Minecraft Loading");
        // setBounds(0, 0, 300, 100);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        progress = new JProgressBar(0, 100);
        contentPane.add(progress, BorderLayout.CENTER);

        pack();
        setSize(400, getHeight());
    }

    public void setProgress() {
        setTitle(SingleProgressBarTracker.getText());

        double p = SingleProgressBarTracker.getProgress();
        progress.setValue((int) (p * 100));
        progress.repaint();
    }
}
