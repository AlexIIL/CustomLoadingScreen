package alexiil.mc.mod.load.json.subtypes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import alexiil.mc.mod.load.baked.BakedFactory;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.factory.BakedFactoryVariableChange;
import alexiil.mc.mod.load.json.JsonFactory;
import alexiil.mc.mod.load.json.JsonRenderingPart;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.NodeType;
import buildcraft.lib.expression.node.value.IVariableNode;
import buildcraft.lib.expression.node.value.NodeVariableLong;

public class JsonFactoryVariableChange extends JsonFactory {
    public final String changeExpression, shouldDestroy;
    public final List<KeptExpr> keptExpressions;

    public JsonFactoryVariableChange(JsonRenderingPart toCreate, String varName, String shouldDestroy, List<KeptExpr> keptExpressions) {
        super(toCreate);
        this.changeExpression = varName;
        this.shouldDestroy = shouldDestroy;
        this.keptExpressions = keptExpressions;
    }

    public JsonFactoryVariableChange(JsonFactoryVariableChange parent, JsonObject obj, JsonDeserializationContext context) {
        super(parent, obj, context);
        this.changeExpression = overrideObject(obj, "change", context, String.class, parent == null ? null : parent.changeExpression, "false");
        this.shouldDestroy = overrideObject(obj, "shouldDestroy", context, String.class, parent == null ? null : parent.shouldDestroy, "false");
        if (obj.has("variables")) {
            JsonElement vars = obj.get("variables");
            if (!vars.isJsonObject()) {
                throw new JsonSyntaxException("Expected an object, got " + vars);
            }
            keptExpressions = new ArrayList<>();
            JsonObject ovars = vars.getAsJsonObject();
            for (Entry<String, JsonElement> entry : ovars.entrySet()) {
                String name = entry.getKey();
                String expr = entry.getValue().getAsString();
                keptExpressions.add(new KeptExpr(name, expr));
            }
            if (parent != null) {
                for (KeptExpr parentExpr : parent.keptExpressions) {
                    boolean found = false;
                    for (KeptExpr real : keptExpressions) {
                        if (real.name.equals(parentExpr.name)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        keptExpressions.add(parentExpr);
                    }
                }
            }
        } else {
            keptExpressions = parent == null ? Collections.emptyList() : parent.keptExpressions;
        }
    }

    @Override
    protected BakedFactory actuallyBake(FunctionContext context) throws InvalidExpressionException {
        IExpressionNode exp = InternalCompiler.compileExpression(changeExpression, context);
        NodeVariableLong varFactoryIndex = context.putVariableLong("factory_index");
        NodeVariableLong varFactoryCount = context.putVariableLong("factory_count");

        List<KeptVariable> keptVariables = new ArrayList<>();
        for (KeptExpr entry : keptExpressions) {
            IExpressionNode node = InternalCompiler.compileExpression(entry.expression, context);
            IVariableNode variable = NodeType.getType(node).makeVariableNode();
            context.putVariable(entry.name, variable);
            keptVariables.add(new KeptVariable(node, variable));
        }

        INodeBoolean _shouldDestroy = GenericExpressionCompiler.compileExpressionBoolean(shouldDestroy, context);
        BakedRenderingPart baked = toCreate.bake(context);
        return new BakedFactoryVariableChange(varFactoryIndex, varFactoryCount, baked, keptVariables, _shouldDestroy, exp, true);
    }

    public static class KeptExpr {
        public final String name;
        public final String expression;

        public KeptExpr(String name, String expression) {
            this.name = name;
            this.expression = expression;
        }
    }

    public static class KeptVariable {
        public final IExpressionNode node;
        public final IVariableNode variable;

        public KeptVariable(IExpressionNode node, IVariableNode variable) {
            this.node = node;
            this.variable = variable;
        }
    }
}
