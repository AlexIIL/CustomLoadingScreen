package alexiil.mc.mod.load;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class Config extends GuiConfig {
    public Config(GuiScreen screen) {
        super(screen, getConfigElements(), Lib.Mod.ID, false, false, "BetterLoadingScreen");
    }

    private static List<IConfigElement> getConfigElements() {
        List<IConfigElement> elements = new ArrayList<IConfigElement>();
        Configuration cfg = CustomLoadingScreen.cfg;
        for (String name : cfg.getCategoryNames()) {
            ConfigCategory cat = cfg.getCategory(name);
            if (!cat.isChild()) elements.add(new ConfigElement(cfg.getCategory(name)));
        }
        return elements;
    }
}
