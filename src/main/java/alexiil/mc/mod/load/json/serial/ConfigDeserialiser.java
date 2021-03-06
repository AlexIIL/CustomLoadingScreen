package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.JsonUtils;

import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonAction;
import alexiil.mc.mod.load.json.JsonConfig;
import alexiil.mc.mod.load.json.JsonFactory;
import alexiil.mc.mod.load.json.JsonRenderingPart;
import alexiil.mc.mod.load.json.JsonVariable;
import alexiil.mc.mod.load.json.JsonVariable.JsonConstant;

import buildcraft.lib.expression.api.InvalidExpressionException;

public enum ConfigDeserialiser implements IThrowingDeserialiser<JsonConfig> {
    INSTANCE;

    @Override
    public JsonConfig deserialize0(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws InvalidExpressionException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            JsonConfig parent = obj.has("parent") ? JsonUtils.deserializeClass(obj, "parent", context, JsonConfig.class) : null;
            JsonRenderingPart[] renders = JsonUtils.deserializeClass(obj, "renders", context, JsonRenderingPart[].class);
            JsonFactory[] factories = JsonUtils.deserializeClass(obj, "factories", context, JsonFactory[].class);
            JsonAction[] actions = JsonUtils.deserializeClass(obj, "actions", context, JsonAction[].class);
            JsonVariable[] constants = obj.has("constants") ? JsonUtils.deserializeClass(obj, "constants", context, JsonConstant[].class) : new JsonVariable[0];
            JsonVariable[] variables = obj.has("variables") ? JsonUtils.deserializeClass(obj, "variables", context, JsonVariable[].class) : new JsonVariable[0];

            List<String[]> functions = new ArrayList<>();
            if (obj.has("functions")) {
                deserializeFunctions(obj.get("functions"), functions);
            }

            JsonConfig cfg = new JsonConfig(parent, renders, functions.toArray(new String[0][]), factories, actions, constants, variables);
            cfg.setSource(obj);
            return cfg;
        } else if (json.isJsonPrimitive()) {
            JsonPrimitive prim = json.getAsJsonPrimitive();
            if (prim.isString()) {
                return ConfigManager.getAsConfig(prim.getAsString());
            } else {
                throw new JsonSyntaxException("Expected an object or a string, found " + prim);
            }
        } else {
            throw new JsonSyntaxException("Expected an object or a string, found " + json);
        }
    }

    private static void deserializeFunctions(JsonElement json, List<String[]> functions) {
        if (!json.isJsonObject()) {
            // A lot of older configs had an array.
            return;
        }
        JsonObject obj = json.getAsJsonObject();
        Set<Entry<String, JsonElement>> entrySet = obj.entrySet();
        for (Entry<String, JsonElement> entry : entrySet) {
            String name = entry.getKey();
            JsonElement jvalue = entry.getValue();
            String value = jvalue.getAsString();
            functions.add(new String[] { name, value });
        }
    }
}
