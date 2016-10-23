package alexiil.mc.mod.load.expression.node.condition;

import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.api.Arguments;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableBoolean;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableString;

public class NodeConditionalString implements INodeString {
    private final INodeBoolean condition;
    private final INodeString ifTrue, ifFalse;

    public NodeConditionalString(INodeBoolean condition, INodeString ifTrue, INodeString ifFalse) {
        this.condition = condition;
        this.ifTrue = ifTrue;
        this.ifFalse = ifFalse;
    }

    @Override
    public String evaluate() {
        return condition.evaluate() ? ifTrue.evaluate() : ifFalse.evaluate();
    }

    @Override
    public INodeString inline(Arguments args) {
        GenericExpressionCompiler.debugStart("Inlining " + this);
        INodeBoolean c = condition.inline(args);
        INodeString t = ifTrue.inline(args);
        INodeString f = ifFalse.inline(args);
        if (c instanceof NodeImmutableBoolean && t instanceof NodeImmutableString && f instanceof NodeImmutableString) {
            NodeImmutableString val = new NodeImmutableString(((NodeImmutableBoolean) c).value ? ((NodeImmutableString) t).value : ((NodeImmutableString) f).value);
            GenericExpressionCompiler.debugEnd("Fully inlined to " + val);
            return val;
        } else if (c != condition || t != ifTrue || f != ifFalse) {
            NodeConditionalString val = new NodeConditionalString(c, t, f);
            GenericExpressionCompiler.debugEnd("Partially inlined to " + val);
            return val;
        } else {
            GenericExpressionCompiler.debugEnd("Unable to inline at all!");
            return this;
        }
    }

    @Override
    public String toString() {
        return "(" + condition + ") ? (" + ifTrue + ") : (" + ifFalse + ")";
    }
}
