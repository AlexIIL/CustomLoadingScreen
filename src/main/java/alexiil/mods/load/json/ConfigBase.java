package alexiil.mods.load.json;

import java.util.HashMap;
import java.util.Map;

import alexiil.mods.load.baked.BakedAction;
import alexiil.mods.load.baked.BakedConfig;
import alexiil.mods.load.baked.BakedRenderingPart;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.IBakedFunction;

public class ConfigBase {
    public final JsonRenderingPart[] render;
    public final JsonFunction[] functions;
    public final JsonFactory[] factories;
    public final JsonAction[] actions;
    public final JsonVariable[] variables;

    public ConfigBase(JsonRenderingPart[] render, JsonFunction[] functions, JsonFactory[] factories, JsonAction[] actions, JsonVariable[] variables) {
        this.render = render;
        this.functions = functions;
        this.factories = factories;
        this.actions = actions;
        this.variables = variables;
    }

    public ConfigBase(ImageRender[] images) {
        render = new JsonRenderingPart[images.length];
        for (int i = 0; i < render.length; i++)
            render[i] = new JsonRenderingPart(images[i], new JsonInstruction[0], "true");
        functions = new JsonFunction[0];
        factories = new JsonFactory[0];
        actions = new JsonAction[0];
        variables = new JsonVariable[0];
    }

    public BakedConfig bake() {
        Map<String, IBakedFunction<?>> functions = new HashMap<String, IBakedFunction<?>>();
        for (JsonFunction func : this.functions) {
            functions.put(func.name, FunctionBaker.bakeFunction(func.function, functions));
        }

        BakedRenderingPart[] array = new BakedRenderingPart[render.length];
        for (int i = 0; i < render.length; i++) {
            array[i] = render[i].bake(functions);
        }

        BakedAction[] actions = new BakedAction[this.actions.length];
        for (int i = 0; i < this.actions.length; i++) {
            actions[i] = this.actions[i].bake(functions);
        }

        return new BakedConfig(array, actions);
    }
}
