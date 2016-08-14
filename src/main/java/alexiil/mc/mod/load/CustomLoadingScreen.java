package alexiil.mc.mod.load;

import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.load.ConfigAccess.IConfigurableMod;

@Mod(modid = Lib.Mod.ID, guiFactory = "alexiil.mc.mod.load.ConfigGuiFactory", acceptableRemoteVersions = "*", clientSideOnly = true)
public class CustomLoadingScreen implements IConfigurableMod {
    public static Configuration cfg;

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        // MinecraftForge.EVENT_BUS.register(CustomLoadingScreen.class);
        // cfg = ConfigAccess.get(event.getSuggestedConfigurationFile(), this).cfg();
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void guiOpen(GuiOpenEvent event) {
        // if (event.getGui() != null && event.getGui() instanceof GuiMainMenu) {
        // ProgressDisplayer.close();
        // }
    }

    @EventHandler
    @SideOnly(Side.SERVER)
    public static void serverAboutToStart(FMLServerAboutToStartEvent event) {
        // ProgressDisplayer.close();
    }

    @Override
    public String modId() {
        return Lib.Mod.ID;
    }
}
