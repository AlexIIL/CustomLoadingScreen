package alexiil.mc.mod.load;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Basic tips manager. Provides access to a single list of */
public class Tips {

    private static final List<String> tips = new ArrayList<>();

    static {
        // Just ensure that nothing can crash by having an empty list
        tips.add("Tips haven't been loaded yet!");
    }

    /** Clears out the current list of tips and sets it to the given {@link List}.
     * <p>
     * This will {@link Collections#shuffle(List)} the tips list or add a default tip if none were loaded. */
    public static void load(List<String> from) {
        tips.clear();
        tips.addAll(from);
        Collections.shuffle(tips);
        if (tips.isEmpty()) {
            tips.add("Tips file was empty!");
        }
    }

    public static String getFirstTip() {
        return tips.get(0);
    }

    public static int getTipCount() {
        return tips.size();
    }

    /** @return The tip at the given index. Wraps around if the index was outside of bounds */
    public static String getTip(int index) {
        int count = tips.size();
        if (index < 0) {
            index = (index % count) + count;
        }
        if (index >= count) {
            // Wrap around as that's more useful than
            index = index % count;
        }
        return tips.get(index);
    }

    public static String getTip(long index) {
        return getTip((int) index);
    }
}
