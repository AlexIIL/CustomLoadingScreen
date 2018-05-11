package alexiil.mc.mod.load.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedFactory;

public abstract class JsonFactory extends JsonConfigurable<JsonFactory, BakedFactory> {
    public final JsonRenderingPart[] toCreate;

    public JsonFactory(JsonRenderingPart[] toCreate) {
        this.toCreate = toCreate;
    }

    public JsonFactory(JsonFactory parent, JsonObject obj, JsonDeserializationContext context) {
        JsonElement elem = obj.get("to_create");
        JsonRenderingPart[] arr;
        if (elem instanceof JsonObject) {
            arr = new JsonRenderingPart[] { context.deserialize(elem, JsonRenderingPart.class) };
        } else if (elem instanceof JsonArray) {
            JsonArray jArray = (JsonArray) elem;
            arr = new JsonRenderingPart[jArray.size()];
            for (int i = 0; i < arr.length; i++) {
                arr[i] = context.deserialize(jArray.get(i), JsonRenderingPart.class);
            }
        } else {
            throw new JsonSyntaxException("Expected to_create as an object or an array, got " + elem + "!");
        }
        toCreate = consolidateArray(parent == null ? null : parent.toCreate, arr);
    }

    @Override
    public void setLocation(ResourceLocation location) {
        super.setLocation(location);
        location = this.resourceLocation;
        for (JsonRenderingPart part : toCreate) {
            part.setLocation(location);
        }
    }
}
