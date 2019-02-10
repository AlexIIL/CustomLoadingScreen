package alexiil.mc.mod.load;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonConfig;
import alexiil.mc.mod.load.progress.SingleProgressBarTracker;
import alexiil.mc.mod.load.progress.SingleProgressBarTracker.LockUnlocker;
import alexiil.mc.mod.load.render.MainSplashRenderer;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableObject;

public class ClsManager {
    public static final Resolution RESOLUTION = new Resolution();

    private static final FunctionContext FUNC_CTX = DefaultContexts.createWithAll();

    private static final NodeVariableObject<String> NODE_STATUS = FUNC_CTX.putVariableString("status");
    private static final NodeVariableObject<String> NODE_STATUS_SUB = FUNC_CTX.putVariableString("sub_status");
    private static final NodeVariableDouble NODE_PERCENTAGE = FUNC_CTX.putVariableDouble("percentage");
    private static final NodeVariableLong NODE_SCREEN_WIDTH = FUNC_CTX.putVariableLong("screen_width");
    private static final NodeVariableLong NODE_SCREEN_HEIGHT = FUNC_CTX.putVariableLong("screen_height");
    private static final NodeVariableDouble NODE_TIME = FUNC_CTX.putVariableDouble("time");
    private static final NodeVariableBoolean NODE_IS_RELOADING = FUNC_CTX.putVariableBoolean("is_reloading");

    private static final NodeVariableObject<String> NODE_ERROR_MESSAGE = FUNC_CTX.putVariableString("error_message");

    private static final List<String> forgeProgressBarTitles = new ArrayList<>();
    private static final List<String> forgeProgressBarMessages = new ArrayList<>();
    private static final List<Double> forgeProgressBarPercents = new ArrayList<>();

    private static MinecraftDisplayerRenderer instance;
    private static IResourceManager resManager;

    static {
        FUNC_CTX.put_l("forge_progress_bar_count", forgeProgressBarTitles::size);
        FUNC_CTX.put_l_o("forge_progress_bar_title", String.class, (index) -> {
            if (index < 0 || index >= forgeProgressBarTitles.size()) {
                return "Invalid Index";
            }
            return forgeProgressBarTitles.get((int) index);
        }).setNeverInline();
        FUNC_CTX.put_l_o("forge_progress_bar_message", String.class, (index) -> {
            if (index < 0 || index >= forgeProgressBarMessages.size()) {
                return "Invalid Index";
            }
            return forgeProgressBarMessages.get((int) index);
        }).setNeverInline();
        FUNC_CTX.put_l_d("forge_progress_bar_percent", (index) -> {
            if (index < 0 || index >= forgeProgressBarPercents.size()) {
                return 0;
            }
            return forgeProgressBarPercents.get((int) index);
        }).setNeverInline();

        FUNC_CTX.put_s("tip", Tips::getFirstTip);
        FUNC_CTX.put_l("tip_count", Tips::getTipCount);
        FUNC_CTX.put_l_o("tip", String.class, Tips::getTip);

        FUNC_CTX.put_l("memory_bytes_total", Runtime.getRuntime()::totalMemory);
        FUNC_CTX.put_l("memory_bytes_max", Runtime.getRuntime()::maxMemory);
        FUNC_CTX.put_l("memory_bytes_free", Runtime.getRuntime()::freeMemory);
        FUNC_CTX.put_l("memory_bytes_used", () -> {
            return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        });

        FUNC_CTX.put_l("memory_total", () -> Runtime.getRuntime().totalMemory() / 1024 / 1024);
        FUNC_CTX.put_l("memory_max", () -> Runtime.getRuntime().maxMemory() / 1024 / 1024);
        FUNC_CTX.put_l("memory_free", () -> Runtime.getRuntime().freeMemory() / 1024 / 1024);
        FUNC_CTX.put_l("memory_used", () -> {
            return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
        });
    }

    public static boolean load() throws InvalidExpressionException {
        resManager = Minecraft.getMinecraft().getResourceManager();
        Minecraft.getMinecraft().refreshResources();

        String used = CustomLoadingScreen.customConfigPath;
        JsonConfig cfg = ConfigManager.getAsConfig(used);
        if (cfg == null) {
            CLSLog.info("Error: couldn't find the config file '" + used + "', defaulting to sample/generic_error");
            NODE_ERROR_MESSAGE.value = "Error: couldn't find the config file '" + used + "'";
            cfg = ConfigManager.getAsConfig("sample/generic_error");
            if (cfg == null) {
                CLSLog.info(
                    "Error: couldn't find the generic error file! '" + used + "', defaulting to sample/generic_error");
                return false;
            }
        } else {
            NODE_ERROR_MESSAGE.value =
                "Unknown error! Check your logs + config file (this should never be shown normally)";
        }

        try {
            instance = new MinecraftDisplayerRenderer(cfg.bake(FUNC_CTX), null);
        } catch (InvalidExpressionException e) {
            CLSLog.warn("Failed to bake " + used, e);
            return false;
        }

        return true;
    }

    public static void renderFrame() {
        try (LockUnlocker u = SingleProgressBarTracker.lockUpdate()) {
            NODE_IS_RELOADING.value = SingleProgressBarTracker.isInReload();
            NODE_STATUS.value = SingleProgressBarTracker.getStatusText();
            NODE_STATUS_SUB.value = SingleProgressBarTracker.getSubStatus();
            NODE_PERCENTAGE.value = SingleProgressBarTracker.getProgress() / SingleProgressBarTracker.MAX_PROGRESS_D;

            Iterator<ProgressBar> i = ProgressManager.barIterator();
            forgeProgressBarTitles.clear();
            forgeProgressBarMessages.clear();
            forgeProgressBarPercents.clear();
            while (i.hasNext()) {
                ProgressBar b = i.next();
                forgeProgressBarTitles.add(b.getTitle());
                forgeProgressBarMessages.add(b.getMessage());
                double div = b.getSteps();
                if (div <= 0) {
                    forgeProgressBarPercents.add(0.0);
                } else {
                    forgeProgressBarPercents.add((b.getStep()) / div);
                }
            }
        }
        RESOLUTION.update();
        NODE_TIME.value = MainSplashRenderer.getTotalTime() / 1000.0;
        instance.render();
    }

    public static boolean renderTransitionFrame() {
        renderFrame();
        return true;
    }

    public static class Resolution {
        private double width, height;
        private double scale = 1;

        private void update() {
            this.width = Display.getWidth();
            this.height = Display.getHeight();
            int scaleFactor = 1;
            boolean unicode = false;
            int guiScale = Minecraft.getMinecraft().gameSettings.guiScale;

            if (guiScale == 0) {
                guiScale = 1000;
            }

            while (scaleFactor < guiScale && width / (scaleFactor + 1) >= 320 && height / (scaleFactor + 1) >= 240) {
                ++scaleFactor;
            }

            if (unicode && scaleFactor % 2 != 0 && scaleFactor != 1) {
                --scaleFactor;
            }

            width = MathHelper.ceil(width / scaleFactor);
            height = MathHelper.ceil(height / scaleFactor);

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
}
