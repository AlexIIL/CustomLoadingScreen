package alexiil.mc.mod.load;

import java.util.Iterator;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

public class SingleProgressBarTracker {
    public static String getText() {
        Iterator<ProgressBar> iter = ProgressManager.barIterator();
        if (iter.hasNext()) {
            ProgressBar bar = iter.next();
            return bar.getTitle() + " - " + bar.getMessage();
        } else {
            return "Minecraft Loading";
        }
    }

    public static double getProgress() {
        Iterator<ProgressBar> iter = ProgressManager.barIterator();
        double min = 0;
        double max = 1;

        int used = 0;
        while (iter.hasNext() & used < 2) {
            ProgressBar bar = iter.next();
            double steps = bar.getSteps() + 1;
            int step = bar.getStep();
            double barMin = step / steps;
            double barMax = barMin + 1 / steps;

            double interpMin = interp(min, max, barMin);
            double interpMax = interp(min, max, barMax);

            min = interpMin;
            max = interpMax;
            used++;
        }

        return min;
    }

    private static double interp(double min, double max, double by) {
        return min * (1 - by) + max * by;
    }
}
