package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;

import com.google.gson.*;

import buildcraft.lib.expression.InvalidExpressionException;

public interface IThrowingDeserialiser<T> extends JsonDeserializer<T> {
    @Override
    default T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return deserialize0(json, typeOfT, context);
        } catch (InvalidExpressionException iee) {
            throw new JsonSyntaxException(iee);
        }
    }

    T deserialize0(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws InvalidExpressionException;
}
