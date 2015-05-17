package alexiil.mods.load.json;

import java.util.Map;

import alexiil.mods.load.baked.func.BakedFunction;

public class JsonVariable {
    public final String name;
    public final String initialValue;

    public JsonVariable(String name, String initialValue) {
        this.name = name;
        this.initialValue = initialValue;
    }

    public void bake(Map<String, BakedFunction<?>> functions) {

    }
}
