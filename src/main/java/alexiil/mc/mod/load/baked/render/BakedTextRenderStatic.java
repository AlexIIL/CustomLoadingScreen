package alexiil.mc.mod.load.baked.render;

import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeLong;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;
import alexiil.mc.mod.load.expression.node.value.NodeMutableLong;
import alexiil.mc.mod.load.expression.node.value.NodeMutableString;

public class BakedTextRenderStatic extends BakedTextRender {
    private final INodeString text;

    public BakedTextRenderStatic(NodeMutableString varText, NodeMutableLong varWidth, NodeMutableLong varHeight, INodeDouble x, INodeDouble y, INodeLong colour, String fontTexture, INodeString text) {
        super(varText, varWidth, varHeight, x, y, colour, fontTexture);
        this.text = text;
    }

    @Override
    public String getText() {
        return text.evaluate();
    }
}
