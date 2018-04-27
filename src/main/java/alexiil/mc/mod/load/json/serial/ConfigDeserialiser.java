package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;

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
            JsonVariable[] variables = JsonUtils.deserializeClass(obj, "variables", context, JsonVariable[].class);
            JsonConfig cfg = new JsonConfig(parent, renders, new String[0], factories, actions, variables);
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
