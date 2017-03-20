package alexiil.mc.mod.load.baked.render;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableString;

public abstract class BakedTextRender extends BakedRenderPositioned {
    protected final NodeVariableString varText;
    protected final INodeDouble x;
    protected final INodeDouble y;
    protected final INodeLong colour;
    protected final String fontTexture;

    public BakedTextRender(NodeVariableString varText, NodeVariableLong varWidth, NodeVariableLong varHeight, INodeDouble x, INodeDouble y, INodeLong colour, String fontTexture) {
        super(varWidth, varHeight);
        this.varText = varText;
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

        font.drawString(text, (float) x.evaluate(), (float) y.evaluate(), 0xFF_00_00_00 | (int) colour.evaluate(), false);
        GlStateManager.color(1, 1, 1, 1);
    }

    public abstract String getText();

    @Override
    public String getLocation() {
        return fontTexture;
    }
}
