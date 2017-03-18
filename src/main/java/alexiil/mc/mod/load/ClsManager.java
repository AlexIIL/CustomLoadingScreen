package alexiil.mc.mod.load;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

import alexiil.mc.mod.load.ModLoadingListener.ModStage;
import alexiil.mc.mod.load.ModLoadingListener.State;
import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonConfig;
import alexiil.mc.mod.load.render.MainSplashRenderer;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableString;

public class ClsManager {
    public static final Resolution RESOLUTION = new Resolution();

    private static final FunctionContext FUNC_CTX = DefaultContexts.createWithAll();

    private static final NodeVariableString NODE_STATUS = FUNC_CTX.putVariableString("status");
    private static final NodeVariableString NODE_STATUS_SUB = FUNC_CTX.putVariableString("sub_status");
    private static final NodeVariableDouble NODE_PERCENTAGE = FUNC_CTX.putVariableDouble("percentage");
    private static final NodeVariableLong NODE_SCREEN_WIDTH = FUNC_CTX.putVariableLong("screenwidth");
    private static final NodeVariableLong NODE_SCREEN_HEIGHT = FUNC_CTX.putVariableLong("screenheight");
    private static final NodeVariableDouble NODE_TIME = FUNC_CTX.putVariableDouble("time");
    private static final NodeVariableBoolean NODE_IS_RELOADING = FUNC_CTX.putVariableBoolean("is_reloading");

    private static MinecraftDisplayerRenderer instance;
    private static IResourceManager resManager;

    private static final int MOD_STAGE_RELOAD = 200;
    private static final int MOD_STAGE_POST = 300;
    private static final int MOD_STAGE_RELOAD_2 = 500;
    private static final int MOD_STAGE_COMPLETE = 1000;

    public static boolean load() {
        resManager = Minecraft.getMinecraft().getResourceManager();
        Minecraft.getMinecraft().refreshResources();

        String used = CustomLoadingScreen.PROP_SCREEN.getString();
        JsonConfig cfg = ConfigManager.getAsConfig(used);
        if (cfg == null) {
            cfg = ConfigManager.getAsConfig("sample/default");
        }
        if (cfg == null) {
            return false;
        }

        try {
            instance = new MinecraftDisplayerRenderer(cfg.bake(FUNC_CTX), null);
        } catch (InvalidExpressionException e) {
            CLSLog.warn("Failed to bake " + used, e);
            return false;
        }

        return true;
    }

    private static final int RELOAD_COUNT = ReloadPart.values().length;
    private static int reloadIndex = -1;
    private static ReloadPart reloadPart = null;
    private static boolean isInReload = false;
    private static boolean reachedPost = false;
    private static boolean hasReloadedAfterPost = false;
    private static ProgressBar lastReloadBar = null;

    public static void renderFrame() {
        RESOLUTION.update();
        NODE_TIME.value = MainSplashRenderer.getTotalTime() / 1000.0;

        String status = "_unknown_";
        String subStatus = "";
        int percentage = 0;

        ModStage stage = ModLoadingListener.stage;
        boolean hasFinishedModLoad = false;
        if (stage != null) {
            status = stage.getDisplayText();
            subStatus = stage.getSubDisplayText();
            if (stage.state == State.POST_INIT && stage.index == 0) {
                isInReload = false;
                reloadIndex = -1;
                reloadPart = null;
            }
            int from, to, parts = 1, part = 0;
            if (!stage.state.isAfterReload1) {
                from = 0;
                to = MOD_STAGE_RELOAD;
                parts = 4;
                part = stage.state.ordinal();
            } else {
                from = MOD_STAGE_POST;
                to = MOD_STAGE_RELOAD_2;
                parts = 2;
                part = stage.state == State.LOAD_COMPLETE ? 1 : 0;
                reachedPost = true;
            }
            int diff = to - from;
            int subDiff = diff / parts;

            if (diff < 0) {
                diff = 0;
            }
            percentage = from + subDiff * part + (subDiff * stage.getSubProgress()) / 1000;
            hasFinishedModLoad = stage.getNext() == stage;
        }

        Iterator<ProgressBar> i = ProgressManager.barIterator();
        List<String> titles = new ArrayList<>();
        boolean foundReload = false;
        while (i.hasNext()) {
            ProgressBar b = i.next();
            String title = b.getTitle();
            titles.add(title);
            ReloadPart part = getReloadPart(titles);
            if (part != null) {
                isInReload = true;
                foundReload = true;
                lastReloadBar = b;
                if (reloadPart != part) {
                    reloadPart = part;
                    reloadIndex++;
                }
                if (hasFinishedModLoad) {
                    hasReloadedAfterPost = true;
                }
            }
        }
        // if (!foundReload && reloadPart != null) {
        // reloadIndex = -1;
        // isInReload = false;
        // }
        if (isInReload) {
            int from, to;
            if (reachedPost) {
                from = MOD_STAGE_RELOAD_2;
                to = MOD_STAGE_COMPLETE;
            } else {
                from = MOD_STAGE_RELOAD;
                to = MOD_STAGE_POST;
            }
            int diff = (to - from) / RELOAD_COUNT;
            percentage = from + (diff * reloadIndex);
            percentage += ((lastReloadBar.getStep() + 1) * diff) / (lastReloadBar.getSteps() + 1);
            subStatus = lastReloadBar.getMessage();
            if (subStatus.length() > 30) {
                subStatus = subStatus.substring(0, 27) + "...";
            }
            status = reloadPart.translatedTitle;
        } else if (hasReloadedAfterPost) {
            percentage = 1000;
            status = Translation.translate("customloadingscreen.finishing");
        }

        NODE_IS_RELOADING.value = isInReload;
        NODE_STATUS.value = status;
        NODE_STATUS_SUB.value = subStatus;
        NODE_PERCENTAGE.value = percentage / 1000.0;
        instance.render();
    }

    private static ReloadPart getReloadPart(List<String> titles) {
        for (ReloadPart part : ReloadPart.values()) {
            if (part.matches(titles)) {
                return part;
            }
        }
        return null;
    }

    public static boolean renderTransitionFrame() {
        renderFrame();
        return true;
    }

    public static class Resolution {
        private double width, height;
        private double scale = 1;

        private void update() {
            ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());

            width = scaled.getScaledWidth();
            height = scaled.getScaledHeight();

            NODE_SCREEN_WIDTH.value = (long) width;
            NODE_SCREEN_HEIGHT.value = (long) height;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        public double getScale() {
            return scale;
        }
    }

    public static IResource getResource(ResourceLocation identifier) throws IOException {
        return resManager.getResource(identifier);
    }

    public enum ReloadPart {
        MODEL_LOADER_BAKE("ModelLoader: baking", "modelloader.bake"),
        MODEL_LOADER_BLOCKS("ModelLoader: blocks", "modelloader.blocks"),
        MODEL_LOADER_ITEMS("ModelLoader: items", "modelloader.items"),
        // Bit odd, but the first stitch is for allocation (In TextureMap)
        // and the second one is in Stitcher itself
        TEXTURE_LOADING("Texture stitching", "texture.load") {
            @Override
            public boolean matches(List<String> titles) {
                for (String s : titles) {
                    if ("Texture creation".equals(s)) {
                        return false;
                    }
                }
                return super.matches(titles);
            }
        },
        TEXTURE_STITCHING("Texture stitching", "texture.stitch") {
            @Override
            public boolean matches(List<String> titles) {
                boolean foundCreation = false;
                for (String s : titles) {
                    if ("Texture creation".equals(s)) {
                        foundCreation = true;
                        break;
                    }
                }
                if (!foundCreation) return false;
                return super.matches(titles);
            }
        };

        public final String barTitle;
        public final String translatedTitle;

        private ReloadPart(String barTitle, String translateKey) {
            this.barTitle = barTitle;
            this.translatedTitle = Translation.translate("customloadingscreen.mcstate." + translateKey);
        }

        public boolean matches(List<String> titles) {
            return titles.get(titles.size() - 1).equals(barTitle);
        }
    }
}
