package alexiil.mods.load.json;

import java.util.HashMap;
import java.util.Map;

import alexiil.mods.load.baked.BakedRenderingPart;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.IBakedFunction;

public class ConfigBase {
    public final JsonRenderingPart[] render;
    public final JsonFunction[] functions;
    public final JsonFactory[] factories;

    public ConfigBase(JsonRenderingPart[] render, JsonFunction[] functions, JsonFactory[] factories) {
        this.render = render;
        this.functions = functions;
        this.factories = factories;
    }

    public ConfigBase(ImageRender[] images) {
        render = new JsonRenderingPart[images.length];
        for (int i = 0; i < render.length; i++)
            render[i] = new JsonRenderingPart(images[i], new JsonInstruction[0], "true");
        functions = new JsonFunction[0];
        factories = new JsonFactory[0];
    }

    public BakedRenderingPart[] bake() {
        Map<String, IBakedFunction<?>> functions = new HashMap<String, IBakedFunction<?>>();
        for (JsonFunction func : this.functions) {
            functions.put(func.name, FunctionBaker.bakeFunction(func.function, functions));
        }

        BakedRenderingPart[] array = new BakedRenderingPart[render.length];
        for (int i = 0; i < render.length; i++) {
            array[i] = render[i].bake(functions);
        }
        return array;
    }
}
