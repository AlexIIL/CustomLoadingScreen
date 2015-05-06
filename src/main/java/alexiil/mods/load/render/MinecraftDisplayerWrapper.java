package alexiil.mods.load.render;

import net.minecraftforge.common.config.Configuration;

import alexiil.mods.load.ProgressDisplayer.IDisplayer;

public class MinecraftDisplayerWrapper implements IDisplayer {
    private MinecraftDisplayer mcDisp;
    private Configuration cfg;
    private boolean hasFailed = false;

    @Override
    public void open(Configuration cfg) {
        this.cfg = cfg;
    }

    @Override
    public void updateProgress(String text, double percent) {
        if (mcDisp == null && !hasFailed) {
            try {
                mcDisp = new MinecraftDisplayer();
                mcDisp.open(cfg);
            }
            catch (Throwable t) {
                System.out.println("Failed to load Minecraft Displayer!");
                t.printStackTrace();
                mcDisp = null;
                hasFailed = true;
            }
            cfg.save();
        }
        if (mcDisp != null)
            mcDisp.updateProgress(text, percent);
    }

    @Override
    public void close() {
        if (mcDisp != null)
            mcDisp.close();
    }

    public static void playFinishedSound() {
        MinecraftDisplayer.playFinishedSound();
    }

    @Override
    public void pause() {
        if (mcDisp != null)
            mcDisp.pause();
    }

    @Override
    public void resume() {
        if (mcDisp != null)
            mcDisp.resume();
    }

    @Override
    public void addFuture(String text, double percent) {
        if (mcDisp != null)
            mcDisp.addFuture(text, percent);
    }

    @Override
    public void pushProgress() {
        if (mcDisp != null)
            mcDisp.pushProgress();
    }

    @Override
    public void popProgress() {
        if (mcDisp != null)
            mcDisp.popProgress();
    }
}
