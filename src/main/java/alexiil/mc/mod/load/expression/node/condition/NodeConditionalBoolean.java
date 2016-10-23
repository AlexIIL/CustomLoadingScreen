package alexiil.mc.mod.load.expression.node.condition;

import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableBoolean;

public class NodeConditionalBoolean implements INodeBoolean {
    private final INodeBoolean condition;
    private final INodeBoolean ifTrue, ifFalse;

    public NodeConditionalBoolean(INodeBoolean condition, INodeBoolean ifTrue, INodeBoolean ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public boolean evaluate() {
        return condition.evaluate() ? ifTrue.evaluate() : ifFalse.evaluate();
    }

    @Override
    public INodeBoolean inline(Arguments args) {
        INodeBoolean c = condition.inline(args);
        INodeBoolean t = ifTrue.inline(args);
        INodeBoolean f = ifFalse.inline(args);
        if (c instanceof NodeImmutableBoolean && t instanceof NodeImmutableBoolean && f instanceof NodeImmutableBoolean) {
            return NodeImmutableBoolean.get(((NodeImmutableBoolean) c).value ? ((NodeImmutableBoolean) t).value : ((NodeImmutableBoolean) f).value);
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            return new NodeConditionalBoolean(c, t, f);
        } else {
            return this;
        }
    }
}
