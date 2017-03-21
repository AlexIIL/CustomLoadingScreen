package alexiil.mc.mod.load.json.serial;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.*;

import net.minecraft.util.JsonUtils;

import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonInsn;
import alexiil.mc.mod.load.json.JsonInsn.*;

import buildcraft.lib.expression.InvalidExpressionException;

public enum InstructionDeserialiser implements IThrowingDeserialiser<JsonInsn> {
    INSTANCE;

    private static final Map<String, TypeDeserialiser> types = new LinkedHashMap<>();
    private static String validTypes = "[]";

    private static void putType(String type, TypeDeserialiser des) {
        types.put(type, des);
        validTypes = Arrays.toString(types.keySet().toArray());
    }

    static {
        putType("builtin/translate", InstructionDeserialiser::deserialiseTranslation);
        putType("builtin/scale", InstructionDeserialiser::deserialiseScale);
        putType("builtin/rotate", InstructionDeserialiser::deserialiseRotation);
        putType("builtin/colour", InstructionDeserialiser::deserialiseColour0);
    }

    @Override
    public JsonInsn deserialize0(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws InvalidExpressionException {
        if (json.isJsonObject()) {
            JsonObject obj = json.getAsJsonObject();
            String type = JsonUtils.getString(obj, "parent");
            TypeDeserialiser des = types.get(type);
            if (des != null) {
                JsonInsn insn = des.deserialize(obj);
                insn.setSource(obj);
                return insn;
            } else {
                throw new JsonSyntaxException("Unknown instruction type" + type + ", should be one of " + validTypes);
            }
        } else if (json.isJsonPrimitive()) {
            JsonPrimitive prim = json.getAsJsonPrimitive();
            if (prim.isString()) {
                return ConfigManager.getAsInsn(prim.getAsString());
            } else {
                throw new JsonSyntaxException("Expected an object or a string, found " + prim);
            }
        } else {
            throw new JsonSyntaxException("Expected an object or a string, found " + json);
        }
    }

    @FunctionalInterface
    private interface TypeDeserialiser {
        JsonInsn deserialize(JsonObject obj) throws JsonParseException;
    }

    private static String getOptionalString(JsonObject obj, String member, String fallback) {
        if (obj.has(member)) {
            return JsonUtils.getString(obj, member);
        } else {
            return fallback;
        }
    }

    private static JsonInsnTranslate deserialiseTranslation(JsonObject obj) {
        String x = JsonUtils.getString(obj, "x");
        String y = JsonUtils.getString(obj, "y");
        String z = getOptionalString(obj, "z", "0");
        return new JsonInsnTranslate(x, y, z);
    }

    private static JsonInsnScale deserialiseScale(JsonObject obj) {
        String x = JsonUtils.getString(obj, "x");
        String y = JsonUtils.getString(obj, "y");
        String z = getOptionalString(obj, "z", "0");
        return new JsonInsnScale(x, y, z);
    }

    private static JsonInsnRotate deserialiseRotation(JsonObject obj) {
        String a = JsonUtils.getString(obj, "angle");
        String x = getOptionalString(obj, "x", "0");
        String y = getOptionalString(obj, "y", "0");
        String z = getOptionalString(obj, "z", "0");
        return new JsonInsnRotate(a, x, y, z);
    }

    private static JsonInsn deserialiseColour0(JsonObject obj) {
        if (obj.has("rgb")) {
            String rgb = JsonUtils.getString(obj, "argb");
            return createFromRgb(rgb);
        } else if (obj.has("argb")) {
            String argb = JsonUtils.getString(obj, "argb");
            return new JsonInsnColourTogether(argb);
        } else {
            String a = obj.has("a") ? JsonUtils.getString(obj, "a") : "0xFF";
            String r = JsonUtils.getString(obj, "r");
            String g = JsonUtils.getString(obj, "g");
            String b = JsonUtils.getString(obj, "b");
            return new JsonInsnColourSplit(a, r, g, b);
        }
    }

    private static JsonInsnColourTogether createFromRgb(String rgb) {
        return new JsonInsnColourTogether("0xFF_00_00_00 | (" + rgb + ")");
    }

    public static JsonInsn deserialiseColour(JsonElement elem) {
        if (elem.isJsonPrimitive() && ((JsonPrimitive) elem).isString()) {
            String argb = elem.getAsString();
            JsonInsnColourTogether insn = createFromRgb(argb);
            insn.setSource(elem);
            return insn;
        } else if (elem.isJsonObject()) {
            JsonInsn insn = deserialiseColour0(elem.getAsJsonObject());
            insn.setSource(elem);
            return insn;
        } else {
            throw new JsonSyntaxException("Expected an object or a string, got " + elem);
        }

    }
}
