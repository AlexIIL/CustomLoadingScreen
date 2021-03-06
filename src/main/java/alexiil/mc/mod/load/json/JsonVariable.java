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
    public final boolean constant;

    public JsonVariable(boolean constant, String name, String value) {

        boolean specified = false;
        boolean specifiedAsConstant = false;

        if (name.startsWith("variable ")) {
            specified = true;
            specifiedAsConstant = false;
        } else if (name.startsWith("constant ") || name.startsWith("const ")) {
            specified = true;
            specifiedAsConstant = true;
        }

        if (specified) {
            this.constant = specifiedAsConstant;
            this.name = name.substring(name.indexOf(" ") + 1);
        } else {
            this.constant = constant;
            this.name = name;
        }

        this.value = value;
    }

    @Override
    protected BakedVariable actuallyBake(FunctionContext context) throws InvalidExpressionException {
        IExpressionNode node = InternalCompiler.compileExpression(value, context);

        IVariableNode variable = NodeTypes.makeVariableNode(NodeTypes.getType(node), name);

        if (constant) {
            node = NodeTypes.createConstantNode(node);
        }

        BakedVariable bv = new BakedVariable(constant, variable, node);
        if (constant) {
            context.putVariable(name, node);
            return bv;
        } else {
            context.putVariable(name, variable);
            return bv;
        }
    }

    /** Just for json processing. */
    public static final class JsonConstant extends JsonVariable {
        public JsonConstant(boolean constant, String name, String value) {
            super(constant, name, value);
        }
    }
}
