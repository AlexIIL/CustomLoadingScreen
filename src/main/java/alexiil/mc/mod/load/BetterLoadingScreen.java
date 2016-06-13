package alexiil.mc.mod.load;

import net.minecraft.client.gui.GuiMainMenu;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.load.ConfigAccess.IConfigurableMod;

@Mod(modid = Lib.Mod.ID, guiFactory = "alexiil.mods.load.ConfigGuiFactory", acceptableRemoteVersions = "*", clientSideOnly = true)
public class BetterLoadingScreen implements IConfigurableMod {
    @Instance(Lib.Mod.ID)
    public static BetterLoadingScreen INSTANCE;
    public static Configuration cfg;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(INSTANCE);
        cfg = ConfigAccess.get(event.getSuggestedConfigurationFile(), this).cfg();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void guiOpen(GuiOpenEvent event) {
        if (event.gui != null && event.gui instanceof GuiMainMenu) ProgressDisplayer.close();
    }

    @EventHandler
    @SideOnly(Side.SERVER)
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        ProgressDisplayer.close();
    }

    @Override
    public String modId() {
        return Lib.Mod.ID;
    }
}
