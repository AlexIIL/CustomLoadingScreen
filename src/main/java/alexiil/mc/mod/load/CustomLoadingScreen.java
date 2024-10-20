package alexiil.mc.mod.load;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Random;

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

@Mod(
    //
    modid = Lib.Mod.ID, //
    guiFactory = "alexiil.mc.mod.load.ConfigGuiFactory", //
    acceptableRemoteVersions = "*", //
    clientSideOnly = true//
)
public class CustomLoadingScreen {
    public static final Configuration CONFIG;

    private static final Property PROP_FRAME;
    private static final Property PROP_USE_CUSTOM;
    // private static final Property PROP_DARK_MODE;
    private static final Property PROP_CONFIG;
    private static final Property PROP_CONFIG_RANDOMS;
    private static final Property PROP_WAIT;
    private static final Property PROP_FPS_LIMIT;
    private static final Property PROP_TEXTURE_CLEAR_INTERVAL;
    private static final Property PROP_DEBUG_RESOURCE_LOADING;

    public static final boolean shouldWait;
    public static final boolean useFrame;
    public static final boolean useCustom;
    public static final boolean darkMode;
    public static final boolean debugResourceLoading;
    public static final String customConfigPath;
    public static final int fpsLimit;
    public static final int textureClearInterval;

    private static FrameDisplayer displayer;

    static {
        CONFIG = new Configuration(new File("./config/customloadingscreen.cfg"));

        PROP_FRAME = CONFIG.get("general", "use_frame", false);
        PROP_USE_CUSTOM = CONFIG.get("general", "use_custom", true);

        PROP_CONFIG = CONFIG.get("general", "screen_config", "builtin/random");
        PROP_CONFIG.setComment(
            "Sets the config to use for the custom loading screen. Use 'builtin/random' for a random loading screen on each load."
                + "\nAlternatively you can prefix this with 'config/' to load from the 'config/customloadingscreen/' directory."
                + "\nOr you can use 'sample/slideshow' to display images from config/customloadingscreen/slideshow_#.png."
                + "\nOr you can set this to 'config/example' to use the default example config."
        );

        String[] defaultRandoms = { "sample/default", "sample/white", "sample/scrolling", "sample/panorama_lower" };
        PROP_CONFIG_RANDOMS = CONFIG.get("general", "random_configs", defaultRandoms);

        PROP_WAIT = CONFIG.get("general", "smooth_init", true);
        PROP_WAIT.setComment(
            "Sleep for a tiny amount of time each mod progress stage to make configs that rely on receiving all mod load stages work a bit better."
        );

        // PROP_DARK_MODE = CONFIG.get("general", "dark_mode", false);
        // PROP_DARK_MODE.setComment("Use dark-mode for loading screens rather than light.");
        darkMode = false;// PROP_DARK_MODE.getBoolean();

        PROP_DEBUG_RESOURCE_LOADING = CONFIG.get("debug", "resource_loading", false);
        debugResourceLoading = PROP_DEBUG_RESOURCE_LOADING.getBoolean();

        PROP_FPS_LIMIT = CONFIG.get("general", "fps_limit", 75);
        PROP_FPS_LIMIT.setComment(
            "The maximum fps to target for the loading screen. The default is 75. Values between 2 and 300 are allowed."
        );
        PROP_FPS_LIMIT.setMinValue(2);
        PROP_FPS_LIMIT.setMaxValue(300);
        fpsLimit = Math.max(2, Math.min(300, PROP_FPS_LIMIT.getInt()));

        PROP_TEXTURE_CLEAR_INTERVAL = CONFIG.get("performance", "texture_clear_interval", 10);
        PROP_TEXTURE_CLEAR_INTERVAL.setMinValue(0);
        PROP_TEXTURE_CLEAR_INTERVAL.setComment(
            "The interval, in seconds, after which textures will be deleted to save memory, at the cost of additional disk reading if they are used later."
                + "\nSet debug.resource_loading to true to log when this occurs."
                + "\nSet to 0 to disable texture clearing."
        );
        textureClearInterval = Math.max(0, PROP_TEXTURE_CLEAR_INTERVAL.getInt(10));

        useCustom = PROP_USE_CUSTOM.getBoolean();

        String customName = PROP_CONFIG.getString();
        if ("builtin/random".equals(customName)) {
            String[] possible = PROP_CONFIG_RANDOMS.getStringList();
            if (possible.length == 0) {
                CLSLog.info("No randoms! Defaulting to sample/generic_error...");
                customConfigPath = "sample/generic_error";
            } else {
                customConfigPath = possible[new Random().nextInt(possible.length)];
            }
        } else {
            customConfigPath = customName == null ? "" : customName;
        }
        useFrame = PROP_FRAME.getBoolean();
        if (useFrame) {
            displayer = new FrameDisplayer();
            displayer.start();
        }

        shouldWait = PROP_WAIT.getBoolean();

        if (CONFIG.hasChanged()) {
            CONFIG.save();
        }

        File clsRoot = new File("./config/customloadingscreen/");

        if (!clsRoot.exists()) {
            clsRoot.mkdir();
        }

        File clsExample = new File(clsRoot, "example.json");

        if (!clsExample.exists()) {

            try (OutputStream out = new FileOutputStream(clsExample)) {
                BufferedOutputStream bos = new BufferedOutputStream(out);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bos));

                writeExampleCfg(bw);

                bw.flush();

            } catch (IOException e) {
                CLSLog.warn("Failed to write the example config file!", e);
            }
        }
    }

    public static void finish() {
        if (displayer != null) {
            displayer.finish();
        }
    }

    @EventHandler
    public static void construct(FMLConstructionEvent event) {
        ModLoadingListener.setup();
        MainSplashRenderer.onReachConstruct();
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

    private static void ln(BufferedWriter bw, String str) throws IOException {
        bw.write(str.replace('#', '"'));
        bw.newLine();
    }

    private static void writeExampleCfg(BufferedWriter bw) throws IOException {
        // Exploded copy of "sample/config/default.json"
        ln(bw, "{");
        ln(bw, "    #renders#: [");
        ln(bw, "        {");
        ln(bw, "            #image#: {");
        ln(bw, "                #parent#: #builtin/panorama#,");
        ln(bw, "                #image#: #textures/gui/title/background/panorama_x.png#");
        ln(bw, "            }");
        ln(bw, "        },");
        ln(bw, "        {");
        ln(bw, "            #image#: {");
        ln(bw, "                #parent#: #builtin/image#,");
        ln(bw, "                #image#: #customloadingscreen:textures/generic/darkened_blur_horizontal_strip.png#,");
        ln(bw, "                #position_type#: #CENTER#,");
        ln(bw, "                #offset_pos#: #CENTER#,");
        ln(bw, "                #position#: {");
        ln(bw, "                    #x#: #0#,");
        ln(bw, "                    #y#: #0#,");
        ln(bw, "                    #width#: #screen_width#,");
        ln(bw, "                    #height#: #100#");
        ln(bw, "                },");
        ln(bw, "                #texture#: {");
        ln(bw, "                    #x#: #0#,");
        ln(bw, "                    #y#: #0#,");
        ln(bw, "                    #width#: #1#,");
        ln(bw, "                    #height#: #1#");
        ln(bw, "                }");
        ln(bw, "            }");
        ln(bw, "        },");
        ln(bw, "        {");
        ln(bw, "            #image#: {");
        ln(bw, "                #parent#:#builtin/image#,");
        ln(bw, "                #image#: #customloadingscreen:textures/progress_bars.png#,");
        ln(bw, "                #position_type#: #CENTER#,");
        ln(bw, "                #offset_pos#: #CENTER#,");
        ln(bw, "                #position#:{");
        ln(bw, "                    #x#: #0#,");
        ln(bw, "                    #y#:#20#,");
        ln(bw, "                    #width#:#182 * 2#,");
        ln(bw, "                    #height#:#20#");
        ln(bw, "                },");
        ln(bw, "                #texture#:{");
        ln(bw, "                    #x#: #0#,");
        ln(bw, "                    #y#: #70 / 256.0#,");
        ln(bw, "                    #width#: #182 / 256.0#,");
        ln(bw, "                    #height#: #10 / 256.0#");
        ln(bw, "                }");
        ln(bw, "            }");
        ln(bw, "        },");
        ln(bw, "        {");
        ln(bw, "            #image#: {");
        ln(bw, "                #parent#: #builtin/image#,");
        ln(bw, "                #image#: #customloadingscreen:textures/progress_bars.png#,");
        ln(bw, "                #position_type#: #CENTER#,");
        ln(bw, "                #offset_pos#: #CENTER#,");
        ln(bw, "                #position#:{");
        ln(bw, "                    #x#:#percentage * 182 - 182#,");
        ln(bw, "                    #y#:#20#,");
        ln(bw, "                    #width#:#percentage * 182 * 2#,");
        ln(bw, "                    #height#:#20#");
        ln(bw, "                },");
        ln(bw, "                #texture#:{");
        ln(bw, "                    #x#:#0#,");
        ln(bw, "                    #y#:#80 / 256.0#,");
        ln(bw, "                    #width#: #percentage * 182 / 256.0#,");
        ln(bw, "                    #height#:#10 / 256.0#");
        ln(bw, "                }");
        ln(bw, "            }");
        ln(bw, "        },");
        ln(bw, "        {");
        ln(bw, "            #image#: {");
        ln(bw, "                #parent#: #builtin/text#,");
        ln(bw, "                #image#: #textures/font/ascii.png#,");
        ln(bw, "                #position_type#: #CENTER#,");
        ln(bw, "                #offset_pos#: #CENTER#,");
        ln(bw, "                #text#: #is_reloading ? status : (status  + ': ' + sub_status)#,");
        ln(bw, "                #position#: {");
        ln(bw, "                    #x#: #0#,");
        ln(bw, "                    #y#: #-20#,");
        ln(bw, "                    #width#: #0#,");
        ln(bw, "                    #height#: #0#");
        ln(bw, "                },");
        ln(bw, "                #colour#:#0xFF_FF_FF_FF#");
        ln(bw, "            }");
        ln(bw, "        },");
        ln(bw, "        {");
        ln(bw, "            #image#: {");
        ln(bw, "                #parent#: #builtin/text#,");
        ln(bw, "                #image#: #textures/font/ascii.png#,");
        ln(bw, "                #position_type#: #CENTER#,");
        ln(bw, "                #offset_pos#: #CENTER#,");
        ln(bw, "                #text#: #is_reloading ? sub_status : ''#,");
        ln(bw, "                #position#: {");
        ln(bw, "                    #x#: #0#,");
        ln(bw, "                    #y#: #0#,");
        ln(bw, "                    #width#: #0#,");
        ln(bw, "                    #height#: #0#");
        ln(bw, "                },");
        ln(bw, "                #colour#:#0xFF_FF_FF_FF#");
        ln(bw, "            }");
        ln(bw, "        },");
        ln(bw, "        {");
        ln(bw, "            #image#: {");
        ln(bw, "                #parent#: #builtin/text#,");
        ln(bw, "                #image#: #textures/font/ascii.png#,");
        ln(bw, "                #position_type#: #CENTER#,");
        ln(bw, "                #offset_pos#: #CENTER#,");
        ln(bw, "                #text#: #(floor(percentage * 100)) + '%'#,");
        ln(bw, "                #position#: {");
        ln(bw, "                    #x#: #0#,");
        ln(bw, "                    #y#: #-10#,");
        ln(bw, "                    #width#: #0#,");
        ln(bw, "                    #height#: #0#");
        ln(bw, "                },");
        ln(bw, "                #colour#:#0xFF_FF_FF_FF#");
        ln(bw, "            }");
        ln(bw, "        }");
        ln(bw, "    ],");
        ln(bw, "    #functions#:[");
        ln(bw, "    ],");
        ln(bw, "    #factories#:[");
        ln(bw, "    ],");
        ln(bw, "    #actions#:[");
        ln(bw, "    ],");
        ln(bw, "    #variables#:{");
        ln(bw, "    }");
        ln(bw, "}");
    }
}
