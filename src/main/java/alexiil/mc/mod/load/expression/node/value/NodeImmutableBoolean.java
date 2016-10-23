package alexiil.mc.mod.load.expression.node.value;

import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;

public enum NodeImmutableBoolean implements INodeBoolean, IImmutableNode {
    TRUE(true),
    FALSE(false);

    public final boolean value;

    private NodeImmutableBoolean(boolean b) {
        this.value = b;
    }

    public static NodeImmutableBoolean get(boolean value) {
        if (value) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    @Override
    public boolean evaluate() {
        return value;
    }

    @Override
    public INodeBoolean inline(Arguments args) {
        return this;
    }

    public NodeImmutableBoolean invert() {
        return get(!value);
    }
}
