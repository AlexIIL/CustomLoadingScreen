package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;

import com.google.gson.*;

import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonFactory;
import alexiil.mc.mod.load.json.subtypes.JsonFactoryVariableChange;

import buildcraft.lib.expression.api.InvalidExpressionException;

public enum FactoryDeserialiser implements IThrowingDeserialiser<JsonFactory> {
    INSTANCE;

    private static final JsonPrimitive BUILTIN_CHANGE = new JsonPrimitive("builtin/change");

    @Override
    public JsonFactory deserialize0(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws InvalidExpressionException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("parent")) {
                JsonFactory factory = deserialise0(context, obj);
                factory.setSource(obj);
                return factory;
            } else {
                throw new JsonSyntaxException("Expected either a builtin parent, or a known existing parent!");
            }
        } else if (json.isJsonPrimitive()) {
            JsonPrimitive prim = json.getAsJsonPrimitive();
            if (prim.isString()) {
                return ConfigManager.getAsFactory(prim.getAsString());
            } else {
                throw new JsonSyntaxException("Expected an object or a string, found " + prim);
            }
        } else {
            throw new JsonSyntaxException("Expected an object or a string, found " + json);
        }
    }

    private static JsonFactory deserialise0(JsonDeserializationContext context, JsonObject obj) {
        JsonElement pe = obj.get("parent");
        if (BUILTIN_CHANGE.equals(pe)) {
            return new JsonFactoryVariableChange(null, obj, context);
        } else {
            JsonFactory parent = context.deserialize(pe, JsonFactory.class);
            if (parent instanceof JsonFactoryVariableChange) {
                return new JsonFactoryVariableChange((JsonFactoryVariableChange) parent, obj, context);
            } else {
                throw new IllegalStateException("Unknown JsonFactory " + parent.getClass());
            }
        }
    }
}
