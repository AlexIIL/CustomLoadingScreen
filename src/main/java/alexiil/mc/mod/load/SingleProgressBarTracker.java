package alexiil.mc.mod.load;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

import alexiil.mc.mod.load.ModLoadingListener.ModStage;
import alexiil.mc.mod.load.ModLoadingListener.State;

public class SingleProgressBarTracker {
    public static final int MAX_PROGRESS = 1 << 20; // about 1,000,000
    public static final double MAX_PROGRESS_D = MAX_PROGRESS;

    private static final int MOD_STAGE_RELOAD = MAX_PROGRESS / 5;
    private static final int MOD_STAGE_POST = MAX_PROGRESS * 3 / 10;
    private static final int MOD_STAGE_RELOAD_2 = MAX_PROGRESS / 2;
    private static final int MOD_STAGE_COMPLETE = MAX_PROGRESS;

    private static final int RELOAD_COUNT = ReloadPart.values().length;
    private static int reloadIndex = -1;
    private static ReloadPart reloadPart = null;
    private static boolean isInReload = false;
    private static boolean reachedPost = false;
    private static boolean hasReloadedAfterPost = false;
    private static ProgressBar lastReloadBar = null;

    private static String status, subStatus;
    private static int progress;

    private static final boolean needsLock = CustomLoadingScreen.useFrame && true;

    public static final Lock updateLock = new ReentrantLock(true);
    private static final LockUnlocker lockUnlocker = () -> updateLock.unlock();
    private static final LockUnlocker no_opUnlocker = () -> {};

    public static LockUnlocker lockUpdate() {
        if (needsLock) {
            updateLock.lock();
            update();
            return lockUnlocker;
        }
        update();
        return no_opUnlocker;
    }

    @FunctionalInterface
    public interface LockUnlocker extends AutoCloseable {
        @Override
        void close();
    }

    private static void update() {
        status = State.CONSTRUCT.translate();
        subStatus = "Custom Loading Screen";
        progress = 0;

        ModStage stage = ModLoadingListener.stage;
        boolean hasFinishedModLoad = false;
        if (stage != null) {
            status = stage.getDisplayText();
            subStatus = stage.getSubDisplayText();
            if (stage.state == State.POST_INIT && stage.index == 0) {
                isInReload = false;
                reloadIndex = -1;
                reloadPart = null;
            }
            int from, to, parts = 1, part = 0;
            if (!stage.state.isAfterReload1) {
                from = 0;
                to = MOD_STAGE_RELOAD;
                parts = 4;
                part = stage.state.ordinal();
            } else {
                from = MOD_STAGE_POST;
                to = MOD_STAGE_RELOAD_2;
                parts = 2;
                part = stage.state == State.LOAD_COMPLETE ? 1 : 0;
                reachedPost = true;
            }
            int diff = to - from;
            int subDiff = diff / parts;

            if (diff < 0) {
                diff = 0;
            }
            progress = from + subDiff * part + stage.getSubProgress(subDiff);
            hasFinishedModLoad = stage.getNext() == stage;
        }

        Iterator<ProgressBar> i = ProgressManager.barIterator();
        List<String> titles = new ArrayList<>();
        while (i.hasNext()) {
            ProgressBar b = i.next();
            String title = b.getTitle();
            titles.add(title);
            ReloadPart part = getReloadPart(titles);
            if (part != null) {
                isInReload = true;
                lastReloadBar = b;
                if (reloadPart != part) {
                    reloadPart = part;
                    reloadIndex++;
                }
                if (hasFinishedModLoad) {
                    hasReloadedAfterPost = true;
                }
            }
        }
        if (isInReload) {
            int from, to;
            if (reachedPost) {
                from = MOD_STAGE_RELOAD_2;
                to = MOD_STAGE_COMPLETE;
            } else {
                from = MOD_STAGE_RELOAD;
                to = MOD_STAGE_POST;
            }
            int diff = (to - from) / RELOAD_COUNT;
            progress = from + (diff * reloadIndex);
            progress += ((lastReloadBar.getStep() + 1) * diff) / (lastReloadBar.getSteps() + 1);
            subStatus = lastReloadBar.getMessage();
            if (subStatus.length() > 30) {
                subStatus = subStatus.substring(0, 27) + "...";
            }
            status = reloadPart.translatedTitle;
        } else if (hasReloadedAfterPost) {
            progress = MAX_PROGRESS;
            status = Translation.translate("customloadingscreen.finishing");
        }
    }

    private static ReloadPart getReloadPart(List<String> titles) {
        for (ReloadPart part : ReloadPart.values()) {
            if (part.matches(titles)) {
                return part;
            }
        }
        return null;
    }

    public static String getStatusText() {
        return status;
    }

    public static String getSubStatus() {
        return subStatus;
    }

    public static int getProgress() {
        return progress;
    }

    public static boolean isInReload() {
        return isInReload;
    }

    public enum ReloadPart {
        MODEL_LOADER_BAKE("ModelLoader: baking", "modelloader.bake"),
        MODEL_LOADER_BLOCKS("ModelLoader: blocks", "modelloader.blocks"),
        MODEL_LOADER_ITEMS("ModelLoader: items", "modelloader.items"),
        // Bit odd, but the first stitch is for allocation (In TextureMap)
        // and the second one is in Stitcher itself
        TEXTURE_LOADING("Texture stitching", "texture.load") {
            @Override
            public boolean matches(List<String> titles) {
                for (String s : titles) {
                    if ("Texture creation".equals(s)) {
                        return false;
                    }
                }
                return super.matches(titles);
            }
        },
        TEXTURE_STITCHING("Texture stitching", "texture.stitch") {
            @Override
            public boolean matches(List<String> titles) {
                boolean foundCreation = false;
                for (String s : titles) {
                    if ("Texture creation".equals(s)) {
                        foundCreation = true;
                        break;
                    }
                }
                if (!foundCreation) return false;
                return super.matches(titles);
            }
        };

        public final String barTitle;
        public final String translatedTitle;

        private ReloadPart(String barTitle, String translateKey) {
            this.barTitle = barTitle;
            this.translatedTitle = Translation.translate("customloadingscreen.mcstate." + translateKey);
        }

        public boolean matches(List<String> titles) {
            return titles.get(titles.size() - 1).equals(barTitle);
        }
    }
}
