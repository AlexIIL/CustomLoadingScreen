package alexiil.mods.load.render;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import alexiil.mods.load.ProgressDisplayer;
import alexiil.mods.load.ProgressDisplayer.IDisplayer;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.json.Area;
import alexiil.mods.load.json.ConfigBase;
import alexiil.mods.load.json.EPosition;
import alexiil.mods.load.json.EType;
import alexiil.mods.load.json.ImageRender;
import alexiil.mods.load.json.JsonConfig;
import alexiil.mods.load.json.JsonFunction;
import alexiil.mods.load.json.JsonInstruction;
import alexiil.mods.load.json.JsonRenderingPart;
import alexiil.mods.load.render.RenderingStatus.ProgressPair;

public class MinecraftDisplayer implements IDisplayer {
    // private static Logger log = LogManager.getLogger("BetterLoadingScreen");
    private static String sound;
    private static String defaultSound = "random.levelup";
    private ConfigBase toDisplay;
    // private TextureManager textureManager = null;
    // private Map<String, FontRenderer> fontRenderers = new HashMap<String, FontRenderer>();
    // private FontRenderer fontRenderer = null;
    // private ScaledResolution resolution = null;
    private Minecraft mc = null;
    // private boolean callAgain = false;
    private volatile String lastText;
    private volatile double lastPercent;
    private IResourcePack myPack;
    private volatile boolean isOpen = true, paused = false;
    private RenderingStatus status;

    public static void playFinishedSound() {
        SoundHandler soundHandler = Minecraft.getMinecraft().getSoundHandler();
        ResourceLocation location = new ResourceLocation(sound);
        SoundEventAccessorComposite snd = soundHandler.getSound(location);
        if (snd == null) {
            System.out.println("The sound given (" + sound + ") did not give a valid sound!");
            location = new ResourceLocation(defaultSound);
            snd = soundHandler.getSound(location);
        }
        if (snd == null) {
            System.out.println("Default sound did not give a valid sound!");
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
                }
                catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    // Because of the wrapper, we can actually use this to create stuffs that we need
    @Override
    public void open(Configuration cfg) {
        mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        status = new RenderingStatus(res.getScaledWidth(), res.getScaledWidth());
        // Open the normal config
        String comment4 = "What sound to play when loading is complete. Default is the level up sound (" + defaultSound + ")";
        sound = cfg.getString("sound", "general", defaultSound, comment4);

        // Add ourselves as a resource pack
        if (!ProgressDisplayer.coreModLocation.isDirectory())
            myPack = new FMLFileResourcePack(ProgressDisplayer.modContainer);
        else
            myPack = new FMLFolderResourcePack(ProgressDisplayer.modContainer);
        getOnlyList().add(myPack);
        mc.refreshResources();

        // Open the special config directory
        File configDir = new File("./config/BetterLoadingScreen");
        if (!configDir.exists())
            configDir.mkdirs();

        // Image Config
        String progress = "betterloadingscreen:textures/progressBars.png";
        String title = "textures/gui/title/mojang.png";
        String font = "textures/font/ascii.png";

        toDisplay =
            new ConfigBase(new ImageRender[] {
                new ImageRender(title, EPosition.CENTER, EType.STATIC, new Area(0, 0, 256, 256), new Area(0, 0, 256, 256)),
                new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_STATUS, null, new Area(0, 30, 0, 0), "000000", null),
                new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(0, 40, 0, 0), "000000", null),
                new ImageRender(progress, EPosition.CENTER, EType.STATIC, new Area(0, 10, 182, 5), new Area(0, 50, 182, 5)),
                new ImageRender(progress, EPosition.CENTER, EType.DYNAMIC_PERCENTAGE, new Area(0, 15, 182, 5), new Area(0, 50, 182, 5)) });

        ConfigBase presetData = toDisplay;

        File imagesFile = new File(configDir, "images.json");
        JsonConfig<ConfigBase> imagesConfig = new JsonConfig<ConfigBase>(imagesFile, ConfigBase.class, toDisplay);
        toDisplay = imagesConfig.load();

        // Preset 1 is the default one
        definePreset(configDir, "preset-default", presetData);

        // Preset 2 uses something akin to minecraft's loading screen when loading a world
        presetData =
            new ConfigBase(new ImageRender[] {
                new ImageRender("textures/gui/options_background.png", EPosition.CENTER, EType.STATIC, new Area(0, 0, 65536, 65536), new Area(0, 0,
                    8192, 8192), "404040", null),
                new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_STATUS, null, new Area(0, 0, 0, 0), "FFFFFF", null),
                new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(0, 10, 0, 0), "FFFFFF", null),
                new ImageRender(font, EPosition.BOTTOM_CENTER, EType.STATIC_TEXT, null, new Area(0, -10, 0, 0), "FFDD49",
                    "Better Loading Screen by AlexIIL") });
        definePreset(configDir, "preset-akin to world loading", presetData);

        // Preset 3 uses something similar to fry's test #5 for FML's loading screen
        presetData =
            new ConfigBase(new ImageRender[] {
                new ImageRender(title, EPosition.CENTER, EType.STATIC, new Area(0, 0, 256, 256), new Area(0, 0, 256, 256)),
                new ImageRender(font, EPosition.CENTER, EPosition.CENTER_LEFT, EType.DYNAMIC_TEXT_STATUS, null, new Area(-91, 10, 0, 0), "000000",
                    null),
                new ImageRender(progress, EPosition.CENTER, EType.STATIC, new Area(0, 50, 182, 10), new Area(0, 40, 182, 10)),
                new ImageRender(progress, EPosition.CENTER, EType.DYNAMIC_PERCENTAGE, new Area(0, 60, 182, 10), new Area(0, 40, 182, 10), "CC0000",
                    null), new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(0, 41, 0, 0), "000000", null) });
        definePreset(configDir, "preset-forge test", presetData);

        // Preset 3 uses rotating cakes. And non rotating cakes.

        String cakeIcon = "textures/items/cake.png";
        String cakeInner = "textures/blocks/cake_inner.png";
        String cakeSide = "textures/blocks/cake_side.png";

        JsonRenderingPart[] parts = new JsonRenderingPart[8];
        // Rotating Cakes:
        parts[0] =
            new JsonRenderingPart(new ImageRender(cakeIcon, EPosition.TOP_LEFT, EType.STATIC, new Area(0, 0, 256, 256), new Area(-32, -32, 64, 64)),
                new JsonInstruction[] { new JsonInstruction("position", new String[] { "128", "128" }),
                    new JsonInstruction("rotate", new String[] { "cake_rotation", "0", "0", "1" }) }, "true");
        parts[1] =
            new JsonRenderingPart(new ImageRender(cakeIcon, EPosition.TOP_RIGHT, EType.STATIC, new Area(0, 0, 256, 256), new Area(-32, -32, 64, 64)),
                new JsonInstruction[] { new JsonInstruction("position", new String[] { "0", "128" }),
                    new JsonInstruction("rotate", new String[] { "cake_rotation", "0", "0", "0-1" }) }, "true");
        parts[2] =
            new JsonRenderingPart(
                new ImageRender(cakeIcon, EPosition.BOTTOM_LEFT, EType.STATIC, new Area(0, 0, 256, 256), new Area(-32, -32, 64, 64)),
                new JsonInstruction[] { new JsonInstruction("position", new String[] { "128", "0" }),
                    new JsonInstruction("rotate", new String[] { "cake_rotation", "0", "0", "1" }) }, "true");
        parts[3] =
            new JsonRenderingPart(new ImageRender(cakeIcon, EPosition.BOTTOM_RIGHT, EType.STATIC, new Area(0, 0, 256, 256),
                new Area(-32, -32, 64, 64)), new JsonInstruction[] { new JsonInstruction("position", new String[] { "0", "0" }),
                new JsonInstruction("rotate", new String[] { "cake_rotation", "0", "0", "0-1" }) }, "true");

        // Texts
        parts[4] =
            new JsonRenderingPart(new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_STATUS, null, new Area(0, -30, 0, 0), "000000", null),
                new JsonInstruction[0], "true");;
        parts[5] =
            new JsonRenderingPart(
                new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(0, -20, 0, 0), "000000", null),
                new JsonInstruction[0], "true");;

        // Cake Bar
        parts[6] =
            new JsonRenderingPart(new ImageRender(cakeInner, EPosition.CENTER, EType.STATIC, new Area(0, 0, 1024, 256), new Area(0, 0, 256, 64)),
                new JsonInstruction[0], "true");
        parts[7] =
            new JsonRenderingPart(new ImageRender(cakeSide, EPosition.CENTER, EType.DYNAMIC_PERCENTAGE, new Area(0, 0, 1024, 256), new Area(0, 0,
                256, 64)), new JsonInstruction[0], "true");

        JsonFunction[] functions = new JsonFunction[] { new JsonFunction("cake_rotation", "seconds * seconds * 50") };

        presetData = new ConfigBase(parts, functions, null);

        definePreset(configDir, "preset-CAKE", presetData);

        // Preset 4 uses an introduction to Custom Main Menu's "End Sidebar" theme

        // TODO: preset 4 :P

        // Preset 5 uses... idk, TODO: Preset 5 etc

        // Open the rendering thread
        final TextureAnimator animator = new TextureAnimator(toDisplay);
        final MinecraftDisplayerRenderer render = new MinecraftDisplayerRenderer(this, toDisplay, animator);
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
                }
                catch (FunctionException fe) {
                    throw new RuntimeException("A function failed!", fe);
                }
                catch (Throwable t) {
                    throw new RuntimeException("Something unexpected happened!", t);
                }
            }
        }, 0, 17);
    }

    private void definePreset(File configDir, String name, ConfigBase images) {
        File presetFile = new File(configDir, name + ".json");
        JsonConfig<ConfigBase> presetConfig = new JsonConfig<ConfigBase>(presetFile, ConfigBase.class, images);
        presetConfig.createNew();
    }

    public void reDisplayProgress() {
        if (isOpen)
            updateProgress(lastText, lastPercent);
    }

    @Override
    public void updateProgress(String text, double percent) {
        lastText = text;
        lastPercent = percent;
        status.progressState.changeFieldProgress(new ProgressPair(text, percent), status.getSeconds());
    }

    @Override
    public void close() {
        isOpen = false;
        getOnlyList().remove(myPack);
    }

    public String getText() {
        return lastText;
    }

    public double getProgress() {
        return lastPercent;
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
        // TODO Auto-generated method stub

    }

    @Override
    public void popProgress() {
        // TODO Auto-generated method stub

    }

    @Override
    public void addFuture(String text, double percent) {
        // TODO Auto-generated method stub

    }

    public RenderingStatus status() {
        return status;
    }
}
