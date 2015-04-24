package alexiil.mods.load.baked;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.render.MinecraftDisplayerRenderer;
import alexiil.mods.load.render.RenderingStatus;

public abstract class BakedTextRender extends BakedRender {
    private final IBakedFunction<Double> x, y, colour;
    private final String fontTexture;

    public BakedTextRender(IBakedFunction<Double> x, IBakedFunction<Double> y, IBakedFunction<Double> colour, String font) {
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
}
