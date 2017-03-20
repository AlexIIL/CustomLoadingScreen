package alexiil.mc.mod.load.json;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.*;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InvalidExpressionException;

public class JsonConfig extends JsonConfigurable<JsonConfig, BakedConfig> {
    public final JsonRenderingPart[] renders;
    public final String[] functions;
    public final JsonFactory[] factories;
    public final JsonAction[] actions;
    public final JsonVariable[] variables;

    public JsonConfig(JsonRenderingPart[] renders, String[] functions, JsonFactory[] factories, JsonAction[] actions, JsonVariable[] variables) {
        this.renders = renders;
        this.functions = functions;
        this.factories = factories;
        this.actions = actions;
        this.variables = variables;
    }

    public JsonConfig(JsonConfig parent, JsonRenderingPart[] renders, String[] functions, JsonFactory[] factories, JsonAction[] actions, JsonVariable[] variables) {
        this.renders = consolidateArray(parent == null ? null : parent.renders, renders);
        this.functions = consolidateArray(parent == null ? null : parent.functions, functions);
        this.factories = consolidateArray(parent == null ? null : parent.factories, factories);
        this.actions = consolidateArray(parent == null ? null : parent.actions, actions);
        this.variables = consolidateArray(parent == null ? null : parent.variables, variables);
    }

    @Override
    public void setLocation(ResourceLocation location) {
        super.setLocation(location);
        location = this.resourceLocation;
        for (JsonVariable v : variables)
            v.setLocation(location);
        for (JsonRenderingPart p : renders)
            p.setLocation(location);
        for (JsonFactory f : factories)
            f.setLocation(location);
        for (JsonAction a : actions)
            a.setLocation(location);
    }

    @Override
    protected BakedConfig actuallyBake(FunctionContext context) throws InvalidExpressionException {
        BakedVariable[] vars = new BakedVariable[variables.length];
        for (int i = 0; i < variables.length; i++) {
            vars[i] = variables[i].bake(context);
        }

        BakedRenderingPart[] array = new BakedRenderingPart[this.renders.length];
        for (int i = 0; i < this.renders.length; i++) {
            JsonRenderingPart jrp = this.renders[i];
            array[i] = jrp.bake(context);
        }

        BakedAction[] actions;
        if (this.actions == null || this.actions.length == 0) {
            actions = new BakedAction[0];
        } else {
            actions = new BakedAction[this.actions.length];
            for (int i = 0; i < this.actions.length; i++) {
                JsonAction ja = this.actions[i];
                actions[i] = ja.bake(context);
            }
        }

        BakedFactory[] factories;
        if (this.factories == null || this.factories.length == 0) {
            factories = new BakedFactory[0];
        } else {
            factories = new BakedFactory[this.factories.length];
            for (int i = 0; i < this.factories.length; i++) {
                JsonFactory jf = this.factories[i];
                factories[i] = jf.bake(context);
            }
        }
        return new BakedConfig(vars, array, actions, factories);
    }
}
