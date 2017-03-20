package alexiil.mc.mod.load.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import alexiil.mc.mod.load.baked.BakedFactory;

public abstract class JsonFactory extends JsonConfigurable<JsonFactory, BakedFactory> {
    public final JsonRenderingPart toCreate;

    public JsonFactory(JsonRenderingPart toCreate) {
        this.toCreate = toCreate;
    }

    public JsonFactory(JsonFactory parent, JsonObject obj, JsonDeserializationContext context) {
        toCreate = overrideObject(obj, "toCreate", context, JsonRenderingPart.class, parent == null ? null : parent.toCreate, null);
    }
}
