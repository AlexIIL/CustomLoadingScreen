package alexiil.mods.load.coremod;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import alexiil.mods.load.ProgressDisplayer;

@IFMLLoadingPlugin.MCVersion("1.8")
public class LoadingScreenLoadPlugin implements IFMLLoadingPlugin {
    // The only reason this coremod exists is this static method: its the first time our code is called
    static {
        ProgressDisplayer.start();
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {}

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
