package alexiil.mc.mod.load.coremod;

import java.io.File;
import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import alexiil.mc.mod.load.Translation;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions({ "alexiil.mc.mod.load.coremod" })
@IFMLLoadingPlugin.SortingIndex(Integer.MAX_VALUE - 80)
public class ClsPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] { ClsTransformer.class.getName() };
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
    public void injectData(Map<String, Object> data) {
        if (!Translation.scanUrlsForTranslations()) {
            File coremodLocation = (File) data.get("coremodLocation");
            if (coremodLocation == null) {
                coremodLocation = new File("./../bin/");
            }
            // Assume this is a dev environment, and that the build dir is in bin, and the test dir has the same parent
            // as
            // the bin dir...
            Translation.scanFileForTranslations(coremodLocation);
        }
        Translation.setTranslator();
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
