package alexiil.mods.load;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundEventAccessorComposite;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.FMLFileResourcePack;
import net.minecraftforge.fml.client.FMLFolderResourcePack;

import org.lwjgl.opengl.GL11;

import alexiil.mods.load.ProgressDisplayer.IDisplayer;
import alexiil.mods.load.json.Area;
import alexiil.mods.load.json.EPosition;
import alexiil.mods.load.json.EType;
import alexiil.mods.load.json.ImageRender;
import alexiil.mods.load.json.JsonConfig;

public class MinecraftDisplayer implements IDisplayer {
    private static String sound;
    private static String defaultSound = "random.levelup";
    private ImageRender[] images;
    private TextureManager textureManager = null;
    private Map<String, FontRenderer> fontRenderers = new HashMap<String, FontRenderer>();
    private FontRenderer fontRenderer = null;
    private ScaledResolution resolution = null;
    private Minecraft mc = null;
    private boolean callAgain = false, isOpen = true;
    private String lastText;
    private double lastPercent;
    private IResourcePack myPack;

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
        images = new ImageRender[5];
        String progress = "betterloadingscreen:textures/progressBars.png";
        String title = "textures/gui/title/mojang.png";
        String font = "textures/font/ascii.png";
        images[0] = new ImageRender(title, EPosition.CENTER, EType.STATIC, new Area(0, 0, 256, 256), new Area(0, 0, 256, 256));
        images[1] = new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_STATUS, null, new Area(0, -30, 0, 0), "000000", null);
        images[2] = new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(0, -40, 0, 0), "000000", null);
        images[3] = new ImageRender(progress, EPosition.CENTER, EType.STATIC, new Area(0, 10, 182, 5), new Area(0, -50, 182, 5));
        images[4] = new ImageRender(progress, EPosition.CENTER, EType.DYNAMIC_PERCENTAGE, new Area(0, 15, 182, 5), new Area(0, -50, 182, 5));

        ImageRender[] defaultImageRender = images;

        File imagesFile = new File(configDir, "images.json");
        JsonConfig<ImageRender[]> imagesConfig = new JsonConfig<ImageRender[]>(imagesFile, ImageRender[].class, images);
        images = imagesConfig.load();

        // Preset one is the default one
        definePreset(configDir, "preset one", defaultImageRender);

        // Preset two uses something akin to minecraft's loading screen when loading a world
        ImageRender[] presetData = new ImageRender[4];
        presetData[0] =
                new ImageRender("textures/gui/options_background.png", EPosition.CENTER, EType.STATIC, new Area(0, 0, 65536, 65536), new Area(0, 0,
                    8192, 8192), "404040", null);
        presetData[1] = new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_STATUS, null, new Area(0, 0, 0, 0), "FFFFFF", null);
        presetData[2] = new ImageRender(font, EPosition.CENTER, EType.DYNAMIC_TEXT_PERCENTAGE, null, new Area(0, -10, 0, 0), "FFFFFF", null);
        presetData[3] =
                new ImageRender(font, EPosition.BOTTOM_CENTER, EType.STATIC_TEXT, null, new Area(0, 10, 0, 0), "FFDD49",
                    "Better Loading Screen by AlexIIL");
        definePreset(configDir, "preset two", presetData);

        // Preset three uses... idk, TODO: Preset 3 etc
    }

    private void definePreset(File configDir, String name, ImageRender... images) {
        File presetFile = new File(configDir, name + ".json");
        JsonConfig<ImageRender[]> presetConfig = new JsonConfig<ImageRender[]>(presetFile, ImageRender[].class, images);
        presetConfig.createNew();
    }

    public void reDisplayProgress() {
        if (isOpen)
            displayProgress(lastText, lastPercent);
    }

    @Override
    public void displayProgress(String text, double percent) {
        lastText = text;
        lastPercent = percent;

        resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        preDisplayScreen();

        for (ImageRender image : images)
            if (image != null)
                drawImageRender(image, text, percent);

        postDisplayScreen();

        if (callAgain) {
            // For some reason, calling this again makes pre-init render properly. I have no idea why, it just does
            callAgain = false;
            reDisplayProgress();
        }
    }

    private FontRenderer fontRenderer(String fontTexture) {
        if (fontRenderers.containsKey(fontTexture))
            return fontRenderers.get(fontTexture);
        FontRenderer font = new FontRenderer(mc.gameSettings, new ResourceLocation(fontTexture), textureManager, false);
        font.onResourceManagerReload(mc.getResourceManager());
        mc.refreshResources();
        font.onResourceManagerReload(mc.getResourceManager());
        fontRenderers.put(fontTexture, font);
        return font;
    }

    public void drawImageRender(ImageRender render, String text, double percent) {
        int startX = render.transformX(resolution.getScaledWidth());
        int startY = render.transformY(resolution.getScaledHeight());
        GlStateManager.color(render.getRed(), render.getGreen(), render.getBlue());
        switch (render.type) {
            case DYNAMIC_PERCENTAGE: {
                ResourceLocation res = new ResourceLocation(render.resourceLocation);
                textureManager.bindTexture(res);
                double alteredWidth = render.position.width * percent;
                drawRect(startX, startY, alteredWidth, render.position.height, render.texture.x, render.texture.y, alteredWidth,
                    render.texture.height);
                break;
            }
            case DYNAMIC_TEXT_PERCENTAGE: {
                FontRenderer font = fontRenderer(render.resourceLocation);
                String percentage = (int) (percent * 100) + "%";
                int width = font.getStringWidth(percentage);
                startX = render.positionType.transformX(render.position.x, resolution.getScaledWidth() - width);
                startY = render.positionType.transformY(render.position.y, resolution.getScaledHeight() - font.FONT_HEIGHT);
                drawString(font, percentage, startX, startY, render.getColour());
                break;
            }
            case DYNAMIC_TEXT_STATUS: {
                FontRenderer font = fontRenderer(render.resourceLocation);
                int width = font.getStringWidth(text);
                startX = render.positionType.transformX(render.position.x, resolution.getScaledWidth() - width);
                startY = render.positionType.transformY(render.position.y, resolution.getScaledHeight() - font.FONT_HEIGHT);
                drawString(font, text, startX, startY, render.getColour());
                break;
            }
            case STATIC_TEXT: {
                FontRenderer font = fontRenderer(render.resourceLocation);
                int width = font.getStringWidth(render.text);
                int startX1 = render.positionType.transformX(render.position.x, resolution.getScaledWidth() - width);
                int startY1 = render.positionType.transformY(render.position.y, resolution.getScaledHeight() - font.FONT_HEIGHT);
                drawString(font, render.text, startX1, startY1, render.getColour());
                break;
            }
            default: {// Assume STATIC
                ResourceLocation res = new ResourceLocation(render.resourceLocation);
                textureManager.bindTexture(res);
                drawRect(startX, startY, render.position.width, render.position.height, render.texture.x, render.texture.y, render.texture.width,
                    render.texture.height);
                break;
            }
        }
    }

    public void drawString(FontRenderer font, String text, int x, int y, int colour) {
        font.drawString(text, x, y, colour);
        GlStateManager.color(1, 1, 1, 1);
    }

    public void drawRect(double x, double y, double drawnWidth, double drawnHeight, double u, double v, double uWidth, double vHeight) {
        float f = 1 / 256F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.startDrawingQuads();
        wr.addVertexWithUV(x, y + drawnHeight, 0, u * f, (v + vHeight) * f);
        wr.addVertexWithUV(x + drawnWidth, y + drawnHeight, 0, (u + uWidth) * f, (v + vHeight) * f);
        wr.addVertexWithUV(x + drawnWidth, y, 0, (u + uWidth) * f, v * f);
        wr.addVertexWithUV(x, y, 0, u * f, v * f);
        tessellator.draw();
    }

    private void preDisplayScreen() {
        if (textureManager == null) {
            textureManager = mc.renderEngine = new TextureManager(mc.getResourceManager());
            mc.refreshResources();
            textureManager.onResourceManagerReload(mc.getResourceManager());
            mc.fontRendererObj = new FontRenderer(mc.gameSettings, new ResourceLocation("textures/font/ascii.png"), textureManager, false);
            if (mc.gameSettings.language != null) {
                LanguageManager lm = mc.getLanguageManager();
                mc.fontRendererObj.setUnicodeFlag(mc.isUnicode());
                mc.fontRendererObj.setBidiFlag(lm.isCurrentLanguageBidirectional());
            }
            mc.fontRendererObj.onResourceManagerReload(mc.getResourceManager());
            callAgain = true;
        }
        if (fontRenderer != mc.fontRendererObj)
            fontRenderer = mc.fontRendererObj;
        if (textureManager != mc.renderEngine)
            textureManager = mc.renderEngine;

        resolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.loadIdentity();
        GlStateManager.ortho(0.0D, (double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.loadIdentity();
        GlStateManager.translate(0.0F, 0.0F, -2000.0F);
        GlStateManager.disableLighting();
        GlStateManager.disableFog();
        GlStateManager.disableDepth();
        GlStateManager.enableTexture2D();

        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.clearColor(1, 1, 1, 1);

        GlStateManager.enableAlpha();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);

        GlStateManager.color(1, 1, 1, 1);
    }

    private void postDisplayScreen() {
        mc.updateDisplay();
    }

    @Override
    public void close() {
        isOpen = false;
        getOnlyList().remove(myPack);
    }
}
