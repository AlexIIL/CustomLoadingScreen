package alexiil.mc.mod.load.progress;

import alexiil.mc.mod.load.ModLoadingListener;
import alexiil.mc.mod.load.progress.SingleProgressBarTracker.ReloadPart;

public class ProgressSectionInfo {
    public final ReloadPart reloadPart;
    public final ModLoadingListener.LoaderStage modState;
    public final String modId;

    public long time;

    public ProgressSectionInfo(ReloadPart reloadPart, long time) {
        this.reloadPart = reloadPart;
        this.modState = null;
        this.modId = null;
        this.time = time;
    }

    public ProgressSectionInfo(ModLoadingListener.LoaderStage modState, String modId, long time) {
        this.reloadPart = null;
        this.modState = modState;
        this.modId = modId;
        this.time = time;
    }

    @Override
    public String toString() {
        String pre;
        if (reloadPart != null) {
            pre = reloadPart.toString();
        } else {
            pre = modState + ": " + modId;
        }
        return pre + " took " + time + "ms";
    }
}
