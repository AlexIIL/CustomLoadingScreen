package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;

import com.google.gson.*;

import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonRender;
import alexiil.mc.mod.load.json.subtypes.JsonRenderImage;
import alexiil.mc.mod.load.json.subtypes.JsonRenderPanorama;
import alexiil.mc.mod.load.json.subtypes.JsonRenderText;

import buildcraft.lib.expression.api.InvalidExpressionException;

public enum ImageDeserialiser implements IThrowingDeserialiser<JsonRender> {
    INSTANCE;

    private static final JsonPrimitive BUILTIN_TEXT = new JsonPrimitive("builtin/text");
    private static final JsonPrimitive BUILTIN_IMAGE = new JsonPrimitive("builtin/image");
    private static final JsonPrimitive BUILTIN_PANORAMA = new JsonPrimitive("builtin/panorama");

    @Override
    public JsonRender deserialize0(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws InvalidExpressionException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            if (obj.has("parent")) {
                JsonRender render = deserialise0(context, obj);
                render.setSource(obj);
                return render;
            } else {
                throw new JsonSyntaxException("Expected either a builtin parent, or a known existing parent!");
            }
        } else if (json.isJsonPrimitive()) {
            JsonPrimitive prim = json.getAsJsonPrimitive();
            if (prim.isString()) {
                return ConfigManager.getAsImage(prim.getAsString());
            } else {
                throw new JsonSyntaxException("Expected an object or a string, found " + prim);
            }
        } else {
            throw new JsonSyntaxException("Expected an object or a string, found " + json);
        }
    }

    private static JsonRender deserialise0(JsonDeserializationContext context, JsonObject obj) {
        JsonElement pe = obj.get("parent");
        if (BUILTIN_TEXT.equals(pe)) {
            return new JsonRenderText(null, obj, context);
        } else if (BUILTIN_IMAGE.equals(pe)) {
            return new JsonRenderImage(null, obj, context);
        } else if (BUILTIN_PANORAMA.equals(pe)) {
            return new JsonRenderPanorama(null, obj, context);
        } else {
            JsonRender parent = context.deserialize(pe, JsonRender.class);
            if (parent instanceof JsonRenderText) {
                return new JsonRenderText((JsonRenderText) parent, obj, context);
            } else if (parent instanceof JsonRenderImage) {
                return new JsonRenderImage((JsonRenderImage) parent, obj, context);
            } else if (parent instanceof JsonRenderPanorama) {
                return new JsonRenderPanorama((JsonRenderPanorama) parent, obj, context);
            } else {
                throw new IllegalStateException("Unknown JsonRender " + parent.getClass());
            }
        }
    }
}
