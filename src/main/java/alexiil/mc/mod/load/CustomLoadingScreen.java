package alexiil.mc.mod.load;

import java.io.File;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import alexiil.mc.mod.load.frame.FrameDisplayer;
import alexiil.mc.mod.load.render.MainSplashRenderer;

@Mod(//
    modid = Lib.Mod.ID,//
    guiFactory = "alexiil.mc.mod.load.ConfigGuiFactory",//
    acceptableRemoteVersions = "*",//
    clientSideOnly = true,//
    dependencies = "required-after:buildcraftlib"//
)
public class CustomLoadingScreen {
    public static final Configuration CONFIG;

    public static final Property PROP_FRAME;
    public static final Property PROP_SCREEN;

    private static FrameDisplayer displayer;

    static {
        CONFIG = new Configuration(new File("./config/customloadingscreen.cfg"));

        PROP_FRAME = CONFIG.get("general", "use_frame", true);
        PROP_SCREEN = CONFIG.get("general", "screen_config", "sample/default");

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
    public static void construct(FMLConstructionEvent event) {
        MainSplashRenderer.onReachConstruct();
        ModLoadingListener.setup();
    }

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(CustomLoadingScreen.class);
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (CONFIG.hasChanged()) {
            CONFIG.save();
        }
    }
}
