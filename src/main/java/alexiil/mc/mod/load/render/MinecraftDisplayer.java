package alexiil.mc.mod.load.render;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import alexiil.mc.mod.load.CLSLog;
import alexiil.mc.mod.load.ProgressDisplayer.IDisplayer;
import alexiil.mc.mod.load.json.JsonConfig;
import alexiil.mc.mod.load.render.RenderingStatus.ProgressPair;

import buildcraft.lib.expression.FunctionContext;

@Deprecated
public class MinecraftDisplayer implements IDisplayer {
    // private static Logger log = LogManager.getLogger("BetterLoadingScreen");
    private static String sound;
    private static String defaultSound = "random.levelup";
    private JsonConfig toDisplay;
    // private TextureManager textureManager = null;
    // private Map<String, FontRenderer> fontRenderers = Maps.newHashMap();
    // private FontRenderer fontRenderer = null;
    // private ScaledResolution resolution = null;
    private Minecraft mc = null;
    // private boolean callAgain = false;
    private IResourcePack myPack;
    private volatile boolean isOpen = true, paused = false;
    private RenderingStatus status;

    public static void playFinishedSound() {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        ResourceLocation location = new ResourceLocation(sound);
        SoundEventAccessor snd = soundHandler.getAccessor(location);
        if (snd == null) {
            CLSLog.warn("The sound given (" + sound + ") did not give a valid sound!");
            location = new ResourceLocation(defaultSound);
            snd = soundHandler.getAccessor(location);
        }
        if (snd == null) {
            CLSLog.warn("Default sound did not give a valid sound!");
            return;
        }
        // ISound sound = PositionedSoundRecord.create(snd);
        // soundHandler.playSound(sound);
    }

    @SuppressWarnings("unchecked")
    private List<IResourcePack> getOnlyList() {
        Field[] flds = mc.getClass().getDeclaredFields();
        for (Field f : flds) {
            if (f.getType().equals(List.class) && !Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                try {
                    return (List<IResourcePack>) f.get(mc);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private static FunctionContext getDefaultMap() {
        FunctionContext functions = new FunctionContext();

        functions.putVariableString("status").value = "Unknown";
        functions.putVariableDouble("percentage").value = 0.34;
        functions.putVariableLong("screenwidth").value = 1280;
        functions.putVariableLong("screenheight").value = 720;
        functions.putVariableDouble("seconds").value = 1.5;

        return functions;
    }

    // Because of the wrapper, we can actually use this to create stuffs that we need
    @Override
    public void open(Configuration cfg) {
        mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc);
        status = new RenderingStatus(res.getScaledWidth(), res.getScaledWidth());
        // Open the normal config
/*
        String comment = "What sound to play when loading is complete. Default is the level up sound (" + defaultSound + ")";
        sound = cfg.getString("sound", "general", defaultSound, comment);

        comment = "The loading screen configuration json file. Some presets are ['sample/default', 'sample/bland', 'sample/rotating_cakes', 'sample/']." + " You can use your own by creating a file in ";
        // TODO: Create a way to have a folder with custom loading screen JSON parts (add a FileResourcePack)
        // TODO: Create sample/default that looks similar to the one with the world background
        // (Panorama, Darkened_blur_horizontal_strip, White status + percentage text + boss loading bar)
        // Or a different sort of loading bar that would fit better?
        String configFile = cfg.getString("jsonDetails", "general", "sample/default", comment);

        // Resource Loader Compat
        loadResourceLoader();

        SplashScreen splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) splashScreen.close();

        // Add ourselves as a resource pack
        if (!ProgressDisplayer.coreModLocation.isDirectory()) myPack = new FMLFileResourcePack(ProgressDisplayer.modContainer);
        else myPack = new FMLFolderResourcePack(ProgressDisplayer.modContainer);
        getOnlyList().add(myPack);
        mc.refreshResources();

        // Image Config
        toDisplay = ConfigManager.getAsConfig(configFile);
        // new JsonConfig("sample/default", null, null, null, null, null);

        // File imagesFile = new File(configDir, "images.json");
        // JsonConfigLoader<JsonConfig> imagesConfig = new JsonConfigLoader<JsonConfig>(imagesFile, JsonConfig.class,
        // toDisplay);
        // toDisplay = imagesConfig.load();
        // toDisplay.resourceLocation = new ResourceLocation("config", "images.json");

        // Use the configs
        BakedConfig baked = toDisplay.bake(getDefaultMap());
        final TextureAnimator animator = new TextureAnimator(baked);
        final MinecraftDisplayerRenderer render = new MinecraftDisplayerRenderer(this, baked, animator);

        // Open the rendering thread
        new Timer("CLS|Renderer").scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isOpen) {// TODO: allow for post loading transitions
                    cancel();
                    // animator.close();
                    render.close();
                    return;
                }

                if (paused) return;

                animator.tick();
                try {
                    render.render();
                } catch (Throwable t) {
                    throw new RuntimeException("Something unexpected happened!", t);
                }
            }
        }, 0, 17);*/
    }

    private void loadResourceLoader() {
        try {
            Class<?> resLoaderClass = Class.forName("lumien.resourceloader.ResourceLoader");
            Object instance = resLoaderClass.newInstance();
            resLoaderClass.getField("INSTANCE").set(null, instance);
            Method m = resLoaderClass.getMethod("preInit", FMLPreInitializationEvent.class);
            m.invoke(instance, new Object[] { null });
        } catch (ClassNotFoundException ex) {
            CLSLog.info("Resource loader not loaded, not initialising early");
        } catch (Throwable t) {
            CLSLog.warn("Resource Loader Compat FAILED!", t);
        }
    }

    @Override
    public void updateProgress(String text, double percent) {
        status.progressState.changeFieldProgress(new ProgressPair(text, percent), status.getSeconds());
    }

    @Override
    public void close() {
        isOpen = false;
        getOnlyList().remove(myPack);
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }

    @Override
    public void pushProgress() {
        status.progressState.pushChild(new ProgressPair("", 0), status.getSeconds());
    }

    @Override
    public void popProgress() {
        status.progressState.popChild(status.getSeconds());
    }

    @Override
    public void addFuture(String text, double percent) {
        // TODO Auto-generated method stub

    }

    public RenderingStatus status() {
        return status;
    }
}
