package alexiil.mc.mod.load.json;

import buildcraft.lib.expression.FunctionContext;

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
