package alexiil.mc.mod.load;

import java.io.IOException;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.ProgressManager;
import net.minecraftforge.fml.common.ProgressManager.ProgressBar;

import alexiil.mc.mod.load.expression.FunctionContext;
import alexiil.mc.mod.load.expression.node.value.NodeMutableDouble;
import alexiil.mc.mod.load.expression.node.value.NodeMutableLong;
import alexiil.mc.mod.load.expression.node.value.NodeMutableString;
import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonConfig;
import alexiil.mc.mod.load.render.MainSplashRenderer;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

public class ClsManager {
    public static final Resolution RESOLUTION = new Resolution();

    private static final FunctionContext FUNCTION_CTX = new FunctionContext();

    private static final NodeMutableString VAR_STATUS = FUNCTION_CTX.getOrAddString("status");
    private static final NodeMutableDouble VAR_PERCENTAGE = FUNCTION_CTX.getOrAddDouble("percentage");
    private static final NodeMutableLong VAR_SCREEN_WIDTH = FUNCTION_CTX.getOrAddLong("screenwidth");
    private static final NodeMutableLong VAR_SCREEN_HEIGHT = FUNCTION_CTX.getOrAddLong("screenheight");
    private static final NodeMutableDouble VAR_SECONDS = FUNCTION_CTX.getOrAddDouble("seconds");

    private static MinecraftDisplayerRenderer instance;
    private static IResourceManager resManager;

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

        instance = new MinecraftDisplayerRenderer(cfg.bake(FUNCTION_CTX), null);

        return true;
    }

    public static void renderFrame() {
        RESOLUTION.update();
        VAR_SECONDS.value = MainSplashRenderer.getTotalTime() / 1000.0;

        String status = "_unknown_";
        double percentage = 0.34;

        Iterator<ProgressBar> i = ProgressManager.barIterator();
        while (i.hasNext()) {
            ProgressBar b = i.next();
            status = b.getTitle() + "-" + b.getMessage();
        }
        VAR_STATUS.value = status;
        VAR_PERCENTAGE.value = percentage;
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
            ScaledResolution scaled = new ScaledResolution(Minecraft.getMinecraft());

            width = scaled.getScaledWidth();
            height = scaled.getScaledHeight();

            VAR_SCREEN_WIDTH.value = (long) width;
            VAR_SCREEN_HEIGHT.value = (long) height;
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
