package alexiil.mc.mod.load.baked.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeLong;
import alexiil.mc.mod.load.expression.node.value.NodeMutableLong;
import alexiil.mc.mod.load.expression.node.value.NodeMutableString;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

public abstract class BakedTextRender extends BakedRender {
    private final NodeMutableString varText;
    private final NodeMutableLong varWidth, varHeight;
    private final INodeDouble x, y;
    private final INodeLong colour;
    private final String fontTexture;

    public BakedTextRender(NodeMutableString varText, NodeMutableLong varWidth, NodeMutableLong varHeight, INodeDouble x, INodeDouble y, INodeLong colour, String fontTexture) {
        this.varText = varText;
        this.varWidth = varWidth;
        this.varHeight = varHeight;
        this.x = x;
        this.y = y;
        this.colour = colour;
        this.fontTexture = fontTexture;
    }

    @Override
    public void render(MinecraftDisplayerRenderer renderer) {
        FontRenderer font = renderer.fontRenderer(fontTexture);
        String text = getText();
        int width = font.getStringWidth(text);
        varText.value = text;
        varWidth.value = width;
        varHeight.value = font.FONT_HEIGHT;

        font.drawString(text, (float) x.evaluate(), (float) y.evaluate(), 0xFF_00_00_00  /*|(int) colour.evaluate()*/, false);
        GlStateManager.color(1, 1, 1, 1);
    }

    public abstract String getText();

    @Override
    public String getLocation() {
        return fontTexture;
    }
}
