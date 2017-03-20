package alexiil.mc.mod.load.json;

import alexiil.mc.mod.load.baked.BakedVariable;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.value.IVariableNode;

public class JsonVariable extends JsonConfigurable<JsonVariable, BakedVariable> {
    public final String name, value;

    public JsonVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    protected BakedVariable actuallyBake(FunctionContext context) throws InvalidExpressionException {
        IExpressionNode node = InternalCompiler.compileExpression(value, context);
        IVariableNode variable = NodeType.getType(node).makeVariableNode();
        context.putVariable(name, variable);
        return new BakedVariable(variable, node);
    }
}
