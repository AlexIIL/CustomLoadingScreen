package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonRenderingPart;

import buildcraft.lib.expression.api.InvalidExpressionException;

public enum RenderingPartDeserialiser implements IThrowingDeserialiser<JsonRenderingPart> {
    INSTANCE;

    @Override
    public JsonRenderingPart deserialize0(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws InvalidExpressionException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            JsonRenderingPart parent = null;
            if (obj.has("parent")) {
                parent = context.deserialize(obj.get("parent"), JsonRenderingPart.class);
            }
            JsonRenderingPart jrp = new JsonRenderingPart(parent, obj, context);
            jrp.setSource(obj);
            return jrp;
        } else if (json.isJsonPrimitive()) {
            JsonPrimitive prim = json.getAsJsonPrimitive();
            if (prim.isString()) {
                return ConfigManager.getAsRenderingPart(prim.getAsString());
            } else {
                throw new JsonSyntaxException("Expected an object or a string, found " + prim);
            }
        } else {
            throw new JsonSyntaxException("Expected an object or a string, found " + json);
        }
    }
}
