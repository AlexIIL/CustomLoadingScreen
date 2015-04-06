package alexiil.mods.load;

import net.minecraft.client.gui.GuiScreen;
import alexiil.mods.lib.git.BaseConfig;

public class Config extends BaseConfig {
    public Config(GuiScreen screen) {
        super(screen, BetterLoadingScreen.instance);
    }
}
