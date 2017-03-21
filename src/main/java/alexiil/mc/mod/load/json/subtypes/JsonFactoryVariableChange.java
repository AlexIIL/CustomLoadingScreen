package alexiil.mc.mod.load.json.subtypes;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedFactory;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.BakedVariable;
import alexiil.mc.mod.load.baked.factory.BakedFactoryVariableChange;
import alexiil.mc.mod.load.json.JsonFactory;
import alexiil.mc.mod.load.json.JsonRenderingPart;
import alexiil.mc.mod.load.json.JsonVariable;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InternalCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.node.value.NodeVariableLong;

public class JsonFactoryVariableChange extends JsonFactory {
    public final String changeExpression, shouldDestroy;
    public final JsonVariable[] variables, keptVariables;

    public JsonFactoryVariableChange(JsonRenderingPart toCreate, String changeExpression, String shouldDestroy, JsonVariable[] variables, JsonVariable[] keptVariables) {
        super(toCreate);
        this.changeExpression = changeExpression;
        this.shouldDestroy = shouldDestroy;
        this.variables = variables;
        this.keptVariables = keptVariables;
    }

    public JsonFactoryVariableChange(JsonFactoryVariableChange parent, JsonObject obj, JsonDeserializationContext context) {
        super(parent, obj, context);
        this.changeExpression = overrideObject(obj, "change", context, String.class, parent == null ? null : parent.changeExpression, "false");
        this.shouldDestroy = overrideObject(obj, "should_destroy", context, String.class, parent == null ? null : parent.shouldDestroy, "false");
        this.variables = overrideVariables(obj, "variables", context, parent == null ? null : parent.variables);
        this.keptVariables = overrideVariables(obj, "kept_variables", context, parent == null ? null : parent.keptVariables);
    }

    @Override
    public void setLocation(ResourceLocation location) {
        super.setLocation(location);
        location = this.resourceLocation;
        for (JsonVariable v : variables)
            v.setLocation(location);
        for (JsonVariable v : keptVariables)
            v.setLocation(location);
    }

    @Override
    protected BakedFactory actuallyBake(FunctionContext context) throws InvalidExpressionException {
        IExpressionNode exp = InternalCompiler.compileExpression(changeExpression, context);
        NodeVariableLong varFactoryIndex = context.putVariableLong("factory_index");
        NodeVariableLong varFactoryCount = context.putVariableLong("factory_count");
        BakedVariable[] _variables = bakeVariables(variables, context);
        BakedVariable[] _keptVariables = bakeVariables(keptVariables, context);

        INodeBoolean _shouldDestroy = GenericExpressionCompiler.compileExpressionBoolean(shouldDestroy, context);
        BakedRenderingPart baked = toCreate.bake(context);
        return new BakedFactoryVariableChange(varFactoryIndex, varFactoryCount, baked, _variables, _keptVariables, _shouldDestroy, exp, true);
    }

    private static BakedVariable[] bakeVariables(JsonVariable[] vars, FunctionContext context) throws InvalidExpressionException {
        BakedVariable[] baked = new BakedVariable[vars.length];
        for (int i = 0; i < vars.length; i++) {
            baked[i] = vars[i].bake(context);
        }
        return baked;
    }
}
