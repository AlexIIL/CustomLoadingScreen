package alexiil.mc.mod.load.json;

import alexiil.mc.mod.load.expression.FunctionContext;

public class JsonVariable {
    public final String name;
    public final String initialValue;

    public JsonVariable(String name, String initialValue) {
        this.name = name;
        this.initialValue = initialValue;
    }

    public void bake(FunctionContext functions) {

    }
}
