package alexiil.mods.load.render;

import java.awt.SplashScreen;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import alexiil.mods.load.BLSLog;
import alexiil.mods.load.ProgressDisplayer;
import alexiil.mods.load.ProgressDisplayer.IDisplayer;
import alexiil.mods.load.baked.BakedConfig;
import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.var.BakedFunctionConstant;
import alexiil.mods.load.baked.func.var.BakedVariablePercentage;
import alexiil.mods.load.baked.func.var.BakedVariableScreenHeight;
import alexiil.mods.load.baked.func.var.BakedVariableScreenWidth;
import alexiil.mods.load.baked.func.var.BakedVariableSeconds;
import alexiil.mods.load.baked.func.var.BakedVariableStatus;
import alexiil.mods.load.json.ConfigManager;
import alexiil.mods.load.json.JsonConfig;
import alexiil.mods.load.render.RenderingStatus.ProgressPair;

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
        SoundEventAccessorComposite snd = soundHandler.getSound(location);
        if (snd == null) {
            BLSLog.warn("The sound given (" + sound + ") did not give a valid sound!");
            location = new ResourceLocation(defaultSound);
            snd = soundHandler.getSound(location);
        }
        if (snd == null) {
            BLSLog.warn("Default sound did not give a valid sound!");
            return;
        }
        ISound sound = PositionedSoundRecord.create(location);
        soundHandler.playSound(sound);
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

    private static Map<String, BakedFunction<?>> getDefaultMap() {
        Map<String, BakedFunction<?>> functions = Maps.newHashMap();

        functions.put("true", new BakedFunctionConstant<Boolean>(true));
        functions.put("false", new BakedFunctionConstant<Boolean>(false));

        functions.put("status", new BakedVariableStatus());
        functions.put("percentage", new BakedVariablePercentage());
        functions.put("screenwidth", new BakedVariableScreenWidth());
        functions.put("screenheight", new BakedVariableScreenHeight());
        functions.put("seconds", new BakedVariableSeconds());

        return functions;
    }

    // Because of the wrapper, we can actually use this to create stuffs that we need
    @Override
    public void open(Configuration cfg) {
        mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        status = new RenderingStatus(res.getScaledWidth(), res.getScaledWidth());
        // Open the normal config

        String comment = "What sound to play when loading is complete. Default is the level up sound (" + defaultSound + ")";
        sound = cfg.getString("sound", "general", defaultSound, comment);

        comment =
            "The loading screen configuration json file. Some presets are ['sample/default', 'sample/bland', 'sample/rotating_cakes', 'sample/']."
                + " You can use your own by creating a file in ";
        // TODO: Create a way to have a folder with custom loading screen JSON parts (add a FileResourcePack)
        // TODO: Create sample/default that looks similar to the one with the world background
        // (Panorama, Darkened_blur_horizontal_strip, White status + percentage text + boss loading bar)
        // Or a different sort of loading bar that would fit better?
        String configFile = cfg.getString("jsonDetails", "general", "sample/default", comment);

        // Resource Loader Compat
        loadResourceLoader();

        SplashScreen splashScreen=SplashScreen.getSplashScreen();
        if(splashScreen!=null)
            splashScreen.close();

        // Add ourselves as a resource pack
        if (!ProgressDisplayer.coreModLocation.isDirectory())
            myPack = new FMLFileResourcePack(ProgressDisplayer.modContainer);
        else
            myPack = new FMLFolderResourcePack(ProgressDisplayer.modContainer);
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
        new Timer("BetterLoadingScreen|Renderer").scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isOpen) {// TODO: allow for post loading transitions
                    cancel();
                    // animator.close();
                    render.close();
                    return;
                }

                if (paused)
                    return;

                animator.tick();
                try {
                    render.tick();
                } catch (FunctionException fe) {
                    throw new RuntimeException("A function failed!", fe);
                } catch (Throwable t) {
                    throw new RuntimeException("Something unexpected happened!", t);
                }
            }
        }, 0, 17);
    }

    private void loadResourceLoader() {
        try {
            Class<?> resLoaderClass = Class.forName("lumien.resourceloader.ResourceLoader");
            Object instance = resLoaderClass.newInstance();
            resLoaderClass.getField("INSTANCE").set(null, instance);
            Method m = resLoaderClass.getMethod("preInit", FMLPreInitializationEvent.class);
            m.invoke(instance, new Object[] { null });
        } catch (ClassNotFoundException ex) {
            BLSLog.info("Resource loader not loaded, not initialising early");
        } catch (Throwable t) {
            BLSLog.warn("Resource Loader Compat FAILED!", t);
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
