package alexiil.mc.mod.load.progress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

import alexiil.mc.mod.load.CustomLoadingScreen;
import alexiil.mc.mod.load.ModLoadingListener;
import alexiil.mc.mod.load.ModLoadingListener.LoaderStage;
import alexiil.mc.mod.load.ModLoadingListener.ModStage;
import alexiil.mc.mod.load.Translation;
import alexiil.mc.mod.load.render.MainSplashRenderer;

public class SingleProgressBarTracker {
    public static final int MAX_PROGRESS = 1 << 20; // about 1,000,000
    public static final double MAX_PROGRESS_D = MAX_PROGRESS;

    private static final int MOD_STAGE_RELOAD = MAX_PROGRESS / 2;
    private static final int MOD_STAGE_POST = MAX_PROGRESS * 9 / 10;
    private static final int MOD_STAGE_COMPLETE = MAX_PROGRESS;

    private static final int RELOAD_COUNT = ReloadPart.values().length;
    private static int reloadIndex = -1;
    private static ReloadPart reloadPart = null;
    private static boolean isInReload = false;
    private static boolean reachedPost = false;
    private static ProgressBar lastReloadBar = null;

    private static String status, subStatus;
    private static int progress;

    private static final boolean needsLock = CustomLoadingScreen.useFrame && true;

    public static final Lock updateLock = new ReentrantLock(true);
    private static final LockUnlocker lockUnlocker = () -> updateLock.unlock();
    private static final LockUnlocker no_opUnlocker = () -> {};

    private static boolean hasExpected;
    private static ProgressSectionInfo[] expected;

    private static final List<ProgressSectionInfo> progressSections = new ArrayList<>();
    private static ProgressSectionInfo currentInfo;

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
        if (!hasExpected) {
            hasExpected = true;
            LongTermProgressTracker tracker = LongTermProgressTracker.load();
            if (tracker != null) {
                if (Arrays.equals(tracker.modIds, ModLoadingListener.modIds.toArray(new String[0]))) {
                    expected = tracker.infos;
                }
            }
        }

        status = LoaderStage.CONSTRUCT.translate();
        subStatus = "Custom Loading Screen";
        progress = 0;

        ModStage stage = ModLoadingListener.stage;
        boolean hasFinishedModLoad = false;
        if (stage != null) {
            status = stage.getDisplayText();
            subStatus = stage.getSubDisplayText();
            if (stage.state == LoaderStage.POST_INIT && stage.index == 0) {
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
                to = MOD_STAGE_COMPLETE;
                parts = 2;
                part = stage.state == LoaderStage.LOAD_COMPLETE ? 1 : 0;
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
            }
        }
        if (isInReload) {
            int from, to;
            from = MOD_STAGE_RELOAD;
            to = MOD_STAGE_POST;
            int diff = (to - from) / RELOAD_COUNT;
            progress = from + (diff * reloadIndex);
            progress += ((lastReloadBar.getStep() + 1) * diff) / (lastReloadBar.getSteps() + 1);
            subStatus = lastReloadBar.getMessage();
            if (subStatus.length() > 30) {
                subStatus = subStatus.substring(0, 27) + "...";
            }
            status = reloadPart.translatedTitle;
        } else if (hasFinishedModLoad) {
            progress = MAX_PROGRESS;
            status = Translation.translate("customloadingscreen.finishing");
        }

        long now = MainSplashRenderer.getTotalTime();
        if (currentInfo != null) {
            if (reloadPart != currentInfo.reloadPart) {
                currentInfo.time = now - currentInfo.time;
                progressSections.add(currentInfo);
                currentInfo = null;
            } else if (!isInReload && stage != null && (stage.state != currentInfo.modState
                || ModLoadingListener.modIds.get(stage.index) != currentInfo.modId)) {
                currentInfo.time = now - currentInfo.time;
                progressSections.add(currentInfo);
                currentInfo = null;
            }
        }
        if (currentInfo == null) {
            if (reloadPart != null) {
                currentInfo = new ProgressSectionInfo(reloadPart, now);
            } else if (stage != null) {
                String modId = ModLoadingListener.modIds.get(stage.index);
                currentInfo = new ProgressSectionInfo(stage.state, modId, now);
            }
        }
    }

    public static List<ProgressSectionInfo> getProgressSections() {
        if (currentInfo != null) {
            currentInfo.time = MainSplashRenderer.getTotalTime() - currentInfo.time;
            progressSections.add(currentInfo);
            currentInfo = null;
        }
        return progressSections;
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
