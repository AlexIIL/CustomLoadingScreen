package alexiil.mc.mod.load.baked.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;
import alexiil.mc.mod.load.render.RenderingStatus;

public abstract class BakedTextRender extends BakedRender {
    private final BakedFunction<Double> x, y, colour;
    private final String fontTexture;

    public BakedTextRender(BakedFunction<Double> x, BakedFunction<Double> y, BakedFunction<Double> colour, String font) {
        this.x = x;
        this.y = y;
        this.colour = colour;
        fontTexture = font;
    }

    @Override
    public void render(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        FontRenderer font = renderer.fontRenderer(fontTexture);
        String text = (String) status.tempVariables.get("text");
        font.drawString(text, (float) (double) x.call(status), (float) (double) y.call(status), (int) (double) colour.call(status), false);
        GlStateManager.color(1, 1, 1, 1);
    }

    @Override
    public void populateVariableMap(RenderingStatus status, MinecraftDisplayerRenderer renderer) throws FunctionException {
        FontRenderer font = renderer.fontRenderer(fontTexture);
        String text = getText(status);
        int width = font.getStringWidth(text);
        status.tempVariables.put("text", text);
        status.tempVariables.put("textwidth", (double) width);
        status.tempVariables.put("textheight", (double) font.FONT_HEIGHT);
    }

    public abstract String getText(RenderingStatus status) throws FunctionException;

    @Override
    public String getLocation() {
        return fontTexture;
    }
}
