package alexiil.mods.load;

import com.mumfrey.liteloader.client.gui.startup.LoadingBar;

public class LiteLoaderProgress extends LoadingBar {
    private static final int NUM_STATES = ModLoadingListener.State.values().length;
    private static final int LITE_LOADER_INIT_ORDINAL = ModLoadingListener.State.LITE_LOADER_INIT.ordinal();
    private static final float LITE_LOADER_START_PERCENT = LITE_LOADER_INIT_ORDINAL / (float) NUM_STATES;

    private String message = "";
    private int totalLiteProgress = 0;
    private int liteProgress = 0;

    @Override
    protected void _dispose() {}

    @Override
    protected void _incLiteLoaderProgress() {
        _incLiteLoaderProgress(message);
    }

    @Override
    protected void _incLiteLoaderProgress(String arg0) {
        message = arg0;
        liteProgress++;
        render();
    }

    @Override
    protected void _incTotalLiteLoaderProgress(int arg0) {
        totalLiteProgress += arg0;
        render();
    }

    private void render() {
        float litePercent = liteProgress / (float) totalLiteProgress;
        litePercent /= (float) NUM_STATES;
        float percent = LITE_LOADER_START_PERCENT + litePercent;
        ProgressDisplayer.displayProgress("LiteLoader: " + message, percent);
    }

    @Override
    protected void _setEnabled(boolean arg0) {}

    @Override
    protected void _setMessage(String arg0) {
        message = arg0;
    }
}
