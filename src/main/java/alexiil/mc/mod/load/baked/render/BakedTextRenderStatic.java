package alexiil.mc.mod.load.baked.render;

import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableObject;

public class BakedTextRenderStatic extends BakedTextRender {
    private final INodeObject<String> text;

    public BakedTextRenderStatic(
        NodeVariableObject<String> varText,
        NodeVariableDouble varWidth,
        NodeVariableDouble varHeight,
        INodeDouble x,
        INodeDouble y,
        INodeLong colour,
        String fontTexture,
        INodeObject<String> text) {
        super(varText, varWidth, varHeight, x, y, colour, fontTexture);
        this.text = text;
    }

    @Override
    public String getText() {
        return text.evaluate();
    }
}
