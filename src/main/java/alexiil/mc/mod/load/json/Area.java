package alexiil.mc.mod.load.json;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.util.JsonUtils;

import alexiil.mc.mod.load.baked.render.BakedArea;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class Area {
    public static final JsonDeserializer<Area> DESERIALISER = Area::deserialise;

    public final String x, y, width, height;

    public Area(double x, double y, double width, double height) {
        this.x = Double.toString(x);
        this.y = Double.toString(y);
        this.width = Double.toString(width);
        this.height = Double.toString(height);
    }

    public Area(String x, String y, String width, String height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "Area [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

    public BakedArea bake(FunctionContext context) throws InvalidExpressionException {
        if (width == null) {
            throw new InvalidExpressionException("Missing 'width'!");
        }
        if (height == null) {
            throw new InvalidExpressionException("Missing 'height'!");
        }
        return new BakedArea(x == null ? "0" : x, y == null ? "0" : y, width, height, context);
    }

    private static Area deserialise(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {

        if (!json.isJsonObject()) {
            throw new JsonParseException("Expected an object, but got " + json);
        }

        JsonObject obj = json.getAsJsonObject();

        String x = JsonUtils.getString(obj, "x", null);
        String y = JsonUtils.getString(obj, "y", null);
        String width = JsonUtils.getString(obj, "width", null);
        String height = JsonUtils.getString(obj, "height", null);

        return new Area(x, y, width, height);
    }
}
