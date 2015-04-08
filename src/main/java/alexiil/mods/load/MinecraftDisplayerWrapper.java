package alexiil.mods.load;

import net.minecraftforge.common.config.Configuration;
import alexiil.mods.load.ProgressDisplayer.IDisplayer;

public class MinecraftDisplayerWrapper implements IDisplayer {
    private MinecraftDisplayer mcDisp;
    private Configuration cfg;

    @Override
    public void open(Configuration cfg) {
        this.cfg = cfg;
    }

    @Override
    public void displayProgress(String text, float percent) {
        if (mcDisp == null) {
            mcDisp = new MinecraftDisplayer();
            mcDisp.open(cfg);
            cfg.save();
        }
        mcDisp.displayProgress(text, percent);
    }

    @Override
    public void close() {
        mcDisp.close();
    }
}
