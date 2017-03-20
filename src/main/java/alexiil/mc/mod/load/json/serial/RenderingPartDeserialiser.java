package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;

import com.google.gson.*;

import net.minecraft.util.JsonUtils;

import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonInsn;
import alexiil.mc.mod.load.json.JsonRender;
import alexiil.mc.mod.load.json.JsonRenderingPart;

public enum RenderingPartDeserialiser implements JsonDeserializer<JsonRenderingPart> {
    INSTANCE;

    @Override
    public JsonRenderingPart deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            JsonRenderingPart parent = JsonUtils.deserializeClass(obj, "parent", context, JsonRenderingPart.class);
            JsonRender image = JsonUtils.deserializeClass(obj, "image", context, JsonRender.class);
            JsonInsn[] instructions = JsonUtils.deserializeClass(obj, "instructions", context, JsonInsn[].class);
            String shouldRender = null;
            if (obj.has("shouldRender")) {
                shouldRender = JsonUtils.getString(obj, "shouldRender");
            }
            JsonRenderingPart jrp = new JsonRenderingPart(parent, image, instructions, shouldRender);
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
