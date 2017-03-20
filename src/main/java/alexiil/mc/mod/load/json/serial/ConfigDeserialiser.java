package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;

import com.google.gson.*;

import net.minecraft.util.JsonUtils;

import alexiil.mc.mod.load.json.*;

public enum ConfigDeserialiser implements JsonDeserializer<JsonConfig> {
    INSTANCE;

    @Override
    public JsonConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            JsonConfig parent = obj.has("parent") ? JsonUtils.deserializeClass(obj, "parent", context, JsonConfig.class) : null;
            JsonRenderingPart[] renders = JsonUtils.deserializeClass(obj, "renders", context, JsonRenderingPart[].class);
            JsonFactory[] factories = JsonUtils.deserializeClass(obj, "factories", context, JsonFactory[].class);
            JsonAction[] actions = JsonUtils.deserializeClass(obj, "actions", context, JsonAction[].class);
            JsonConfig cfg = new JsonConfig(parent, renders, new String[0], factories, actions, new JsonVariable[0]);
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
}
