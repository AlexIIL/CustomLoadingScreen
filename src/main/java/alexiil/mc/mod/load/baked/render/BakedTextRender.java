package alexiil.mc.mod.load.baked.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.FontRenderer;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableObject;

public abstract class BakedTextRender extends BakedRenderPositioned {
    protected final NodeVariableObject<String> varText;
    protected final INodeDouble scale;
    protected final INodeDouble x;
    protected final INodeDouble y;
    protected final INodeLong colour;
    protected final String fontTexture;
    private String _text;
    private double _scale;
    private double _width;
    private long _colour;
    private double _x, _y;

    public BakedTextRender(
        NodeVariableObject<String> varText, NodeVariableDouble varWidth, NodeVariableDouble varHeight,
        INodeDouble scale, INodeDouble x, INodeDouble y, INodeLong colour, String fontTexture
    ) {
        super(varWidth, varHeight);
        this.varText = varText;
        this.scale = scale;
        this.x = x;
        this.y = y;
        this.colour = colour;
        this.fontTexture = fontTexture;
    }

    @Override
    public void evaluateVariables(MinecraftDisplayerRenderer renderer) {
        _text = getText();
        _scale = scale.evaluate();
        FontRenderer font = renderer.fontRenderer(fontTexture);
        _width = (int) (font.getStringWidth(_text) * _scale);
        varWidth.value = _width;
        varHeight.value = font.FONT_HEIGHT * _scale;
        _x = x.evaluate();
        _y = y.evaluate();
        _colour = colour.evaluate();
        if ((_colour & 0xFF_00_00_00) == 0) {
            _colour |= 0xFF_00_00_00;
        } else if ((_colour & 0xFF_00_00_00) == 0x01_00_00_00) {
            _colour &= 0xFF_FF_FF;
        }
    }

    @Override
    public void render(MinecraftDisplayerRenderer renderer) {
        FontRenderer font = renderer.fontRenderer(fontTexture);
        GL11.glPushMatrix();
        GL11.glTranslated(_x, _y, 0);
        GL11.glScaled(_scale, _scale, _scale);
        font.drawString(_text, 0, 0, (int) _colour, false);
        GL11.glPopMatrix();
        GL11.glColor4f(1, 1, 1, 1);
    }

    public abstract String getText();

    @Override
    public String getLocation() {
        return fontTexture;
    }
}
