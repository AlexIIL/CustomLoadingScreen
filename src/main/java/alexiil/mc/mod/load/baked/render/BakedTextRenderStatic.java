package alexiil.mc.mod.load.baked.render;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableString;

public class BakedTextRenderStatic extends BakedTextRender {
    private final INodeString text;

    public BakedTextRenderStatic(
        NodeVariableString varText,
        NodeVariableDouble varWidth,
        NodeVariableDouble varHeight,
        INodeDouble x,
        INodeDouble y,
        INodeLong colour,
        String fontTexture,
        INodeString text) {
        super(varText, varWidth, varHeight, x, y, colour, fontTexture);
        this.text = text;
    }

    @Override
    public String getText() {
        return text.evaluate();
    }
}
