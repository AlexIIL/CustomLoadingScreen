package alexiil.mc.mod.load;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

/** Basic tips manager. Provides access to a single list of tips. */
public class Tips {

    private static final List<String> tips = new ArrayList<>();
    private static boolean anyTips = false;

    static {
        // Just ensure that nothing can crash by having an empty list
        tips.add("Tips haven't been loaded yet!");
    }

    public static void load() {
        File f = new File("config/customloadingscreen_tips.txt");
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            load(parseTips(br));
        } catch (FileNotFoundException e) {
            CLSLog.info("No tip file found at " + f);
        } catch (IOException e) {
            CLSLog.warn("Failed to load the tips file: " + f, e);
        }
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
            anyTips = false;
        } else {
            anyTips = true;
        }
    }

    public static void parseTips(BufferedReader from, List<String> to) throws IOException {
        String line;
        while ((line = from.readLine()) != null) {
            if (line.isEmpty() || line.startsWith("#")) {
                // Comment
            } else {
                to.add(line);
            }
        }
    }

    public static List<String> parseTips(BufferedReader from) throws IOException {
        List<String> list = new ArrayList<>();
        parseTips(from, list);
        return list;
    }

    public static String getFirstTip() {
        return tips.get(0);
    }

    /** Checks to see if any valid tips have been loaded ({@link #getFirstTip()} will return the default tip if this
     * returns false). */
    public static boolean hasAnyTips() {
        return anyTips;
    }

    public static int getTipCount() {
        return tips.size();
    }

    /** @return The tip at the given index, or null if the index is out of bounds. */
    @Nullable
    public static String getTipAt(int index) {
        if (index < 0 || index >= tips.size()) {
            return null;
        }
        return tips.get(index);
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
