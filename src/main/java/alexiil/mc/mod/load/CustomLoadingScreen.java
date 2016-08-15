package alexiil.mc.mod.load;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import alexiil.mc.mod.load.frame.FrameDisplayer;

@Mod(modid = Lib.Mod.ID, guiFactory = "alexiil.mc.mod.load.ConfigGuiFactory", acceptableRemoteVersions = "*", clientSideOnly = true)
public class CustomLoadingScreen {
    public static final Configuration CONFIG;

    public static final Property PROP_FRAME;

    private static FrameDisplayer displayer;

    static {
        CONFIG = new Configuration(new File("./config/customloadingscreen.cfg"));

        PROP_FRAME = CONFIG.get("general", "use_frame", true);

        if (PROP_FRAME.getBoolean()) {
            displayer = new FrameDisplayer();
            displayer.start();
        }

        if (CONFIG.hasChanged()) {
            CONFIG.save();
        }
    }

    public static void finish() {
        if (displayer != null) {
            displayer.finish();
        }
    }

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(CustomLoadingScreen.class);
    }

    @EventHandler
    @SideOnly(Side.SERVER)
    public static void serverAboutToStart(FMLServerAboutToStartEvent event) {

    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (CONFIG.hasChanged()) {
            CONFIG.save();
        }
    }
}
