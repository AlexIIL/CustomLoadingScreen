package alexiil.mods.load;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

@SuppressWarnings("serial")
public class LoadingFrame extends JFrame {
    private static class ProgressPart {
        private final JPanel panel, container;
        private final JLabel label;
        private final JProgressBar bar;
        private final ProgressPart parent;
        private ProgressPart child;

        public ProgressPart(ProgressPart parent) {
            panel = new JPanel();
            panel.setLayout(new BorderLayout(0, 0));

            container = new JPanel();
            container.setLayout(new BorderLayout(0, 0));
            container.setBorder(new LineBorder(Color.BLACK));
            panel.add(container, BorderLayout.NORTH);

            label = new JLabel();
            container.add(label, BorderLayout.NORTH);

            bar = new JProgressBar();
            container.add(bar, BorderLayout.SOUTH);

            this.parent = parent;

            if (parent != null) {
                parent.panel.add(panel, BorderLayout.SOUTH);
            }
        }
    }

    private ProgressPart head, current;

    /** Launch the application. */
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

    /** Create the frame. */
    public LoadingFrame() {
        setTitle("Minecraft Loading");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setBounds(100, 100, 450, 85);
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        contentPane.add(panel, BorderLayout.NORTH);
        panel.setLayout(new BorderLayout(0, 0));

        head = new ProgressPart(null);
        current = head;
    }

    public void setMessage(String message) {
        current.label.setText(message);
    }

    public void setProgress(double percent) {
        current.bar.setValue((int) percent);
    }

    public void pushProgress() {
        current = new ProgressPart(current);
    }

    public void popProgress() {
        if (current == head) {
            throw new Error("Tried to pop the head off!");
        }
        ProgressPart part = current;
        current = current.parent;
        current.panel.remove(part.panel);
        current.panel.revalidate();
        current.panel.repaint();
    }
}
