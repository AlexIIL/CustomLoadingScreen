package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import alexiil.mc.mod.load.json.JsonVariable;

import buildcraft.lib.expression.api.InvalidExpressionException;

public enum VariableArrayDeserialiser implements IThrowingDeserialiser<JsonVariable[]> {
    CONSTANTS(true),
    VARIABLES(false);

    final boolean constant;

    VariableArrayDeserialiser(boolean constant) {
        this.constant = constant;
    }

    @Override
    public JsonVariable[] deserialize0(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws InvalidExpressionException {
        JsonObject obj = json.getAsJsonObject();
        Set<Entry<String, JsonElement>> entrySet = obj.entrySet();
        JsonVariable[] vars = new JsonVariable[entrySet.size()];
        int i = 0;
        for (Entry<String, JsonElement> entry : entrySet) {
            String name = entry.getKey();
            JsonElement jvalue = entry.getValue();
            String value = jvalue.getAsString();
            JsonVariable var = new JsonVariable(constant, name, value);
            var.setSource(jvalue);
            vars[i++] = var;
        }
        return vars;
    }
}
