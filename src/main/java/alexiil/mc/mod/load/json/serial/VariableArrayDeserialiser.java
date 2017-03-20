package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.*;

import alexiil.mc.mod.load.json.JsonVariable;

public enum VariableArrayDeserialiser implements JsonDeserializer<JsonVariable[]> {
    INSTANCE;

    @Override
    public JsonVariable[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        Set<Entry<String, JsonElement>> entrySet = obj.entrySet();
        JsonVariable[] vars = new JsonVariable[entrySet.size()];
        int i = 0;
        for (Entry<String, JsonElement> entry : entrySet) {
            String name = entry.getKey();
            JsonElement jvalue = entry.getValue();
            String value = jvalue.getAsString();
            JsonVariable var = new JsonVariable(name, value);
            var.setSource(jvalue);
            vars[i++] = var;
        }
        return vars;
    }
}
