package alexiil.mc.mod.load.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import alexiil.mc.mod.load.CLSLog;
import alexiil.mc.mod.load.json.ConfigManager.EType;

public class LocationDeserialiser<T extends JsonConfigurable<T, ?>> implements JsonDeserializer<T> {
    private final Class<T> clazz;
    private final EType type;

    public LocationDeserialiser(EType type, Class<T> clazz) {
        this.clazz = clazz;
        this.type = type;
    }

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        CLSLog.info("Deserializing " + json + " as " + typeOfT);
        if (json.isJsonPrimitive()) { // Get the value from the config
            String location = json.getAsString();
            return ConfigManager.getAsT(type, location);
        } else if (json.isJsonObject()) {
            return ConfigManager.getGsonExcluding(clazz).fromJson(json, clazz);
        } else throw new JsonParseException("Must either be a string of the location, or the actual object definition!");
    }
}
