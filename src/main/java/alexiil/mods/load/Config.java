package alexiil.mods.load;

import net.minecraft.client.gui.GuiScreen;

import alexiil.mods.lib.gui.BaseConfig;

public class Config extends BaseConfig {
    public Config(GuiScreen screen) {
        super(screen, BetterLoadingScreen.INSTANCE);
    }
}
