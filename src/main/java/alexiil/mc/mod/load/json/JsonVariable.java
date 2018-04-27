package alexiil.mc.mod.load.json;

import alexiil.mc.mod.load.baked.BakedVariable;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IVariableNode;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.api.NodeTypes;

public class JsonVariable extends JsonConfigurable<JsonVariable, BakedVariable> {
    public final String name, value;

    public JsonVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    protected BakedVariable actuallyBake(FunctionContext context) throws InvalidExpressionException {
        IExpressionNode node = InternalCompiler.compileExpression(value, context);
        IVariableNode variable = NodeTypes.makeVariableNode(NodeTypes.getType(node), name);
        context.putVariable(name, variable);
        return new BakedVariable(variable, node);
    }
}
