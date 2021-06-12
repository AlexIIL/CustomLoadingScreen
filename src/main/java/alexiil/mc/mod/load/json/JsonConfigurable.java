package alexiil.mc.mod.load.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedConfigurable;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.InvalidExpressionException;

/** @param <C> The class that extends this. This is what it should consolidate down to.
 * @param <B> The class that this is baked to. */
public abstract class JsonConfigurable<C extends JsonConfigurable<C, B>, B extends BakedConfigurable> {
    /** This is used for debugging and is initialised from ConfigManager.getAsT() */
    public transient ResourceLocation resourceLocation;
    private transient String rawText;

    public void setLocation(ResourceLocation location) {
        if (resourceLocation == null) {
            resourceLocation = location;
        }
    }

    public final void setSource(String src) {
        if (rawText == null) {
            rawText = src;
        }
    }

    public final void setSource(JsonElement json) {
        if (rawText == null) {
            rawText = ConfigManager.GSON_DEFAULT.toJson(json);
        }
    }

    public String getRawText() {
        return this.rawText;
    }

    /** Bakes this into something that can be ticked quickly. This is a potentially expensive function, so don't call
     * this more than you need to. (This does not cache, as this could potentially be called with a different map of
     * functions) */
    public final B bake(FunctionContext context) throws InvalidExpressionException {
        if (resourceLocation == null) throw new NullPointerException("resourcelocation");
        if (rawText == null) throw new NullPointerException("src in " + resourceLocation);
        try {
            B b = actuallyBake(context);
            b.setOrigin(resourceLocation, rawText);
            return b;
        } catch (InvalidExpressionException iee) {
            throw new InvalidSourceException(resourceLocation, rawText, iee);
        }
    }

    /** Bakes this object into something that will process quickly. Only call this on a consolidated object! (Use
     * {@link #bake(buildcraft.lib.expression.FunctionContext)} instead of this) */
    protected abstract B actuallyBake(FunctionContext context) throws InvalidExpressionException;

    // These are helper methods to make deserialisation one-liners

    protected static <O> O deserialiseObject(
        JsonObject obj, String memeber, JsonDeserializationContext ctx, Class<O> clazz
    ) {
        if (obj.has(memeber)) {
            return ctx.deserialize(obj.get(memeber), clazz);
        }
        return null;
    }

    protected static String consolidateFunction(
        JsonObject obj, String memeber, JsonDeserializationContext ctx, String parent, String defaultF
    ) {
        return consolidateFunction(deserialiseObject(obj, memeber, ctx, String.class), parent, defaultF);
    }

    protected static String consolidateFunction(String in, String parent, String defaultF) {
        if (in == null) {
            if (parent == null) {
                return defaultF;
            } else {
                return parent;
            }
        } else if (parent == null) {
            return in;
        } else {
            return in.replaceAll("super", "(" + parent + ")");
        }
    }

    /** This will override the parents version of the object if in is not null. If in is null and the parent is null,
     * then the default is returned. */
    protected static <O> O overrideObject(
        JsonObject obj, String memeber, JsonDeserializationContext ctx, Class<O> clazz, O parent, O defaultO
    ) {
        O in = deserialiseObject(obj, memeber, ctx, clazz);
        if (in != null) return in;
        if (parent != null) return parent;
        return defaultO;
    }

    protected static JsonVariable[] overrideVariables(
        JsonObject obj, String memeber, JsonDeserializationContext ctx, JsonVariable[] parent
    ) {
        JsonVariable[] in = deserialiseObject(obj, memeber, ctx, JsonVariable[].class);
        if (in == null) {
            return parent == null ? new JsonVariable[0] : parent;
        } else if (parent == null) {
            return in;
        }
        List<JsonVariable> vars = new ArrayList<>(in.length + parent.length);
        Collections.addAll(vars, in);
        for (JsonVariable vParent : parent) {
            boolean found = false;
            for (JsonVariable vChild : vars) {
                if (vChild.name.equalsIgnoreCase(vParent.name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                vars.add(vParent);
            }
        }
        return vars.toArray(new JsonVariable[vars.size()]);
    }

    protected static void ensureExists(Object obj, String name) throws InvalidExpressionException {
        if (obj == null) {
            throw new InvalidExpressionException("Missing element '" + name + "'");
        }
    }

    /** This will effectively combine the two arrays together. Specifically, if both of the arrays are null, null is
     * returned. If both of them are either null or empty (but one is not null) then the non-null one is returned. If
     * both of them are not empty, then a new array is returned with the first array occupying the first positions, and
     * and the second array occupying the last positions. */
    protected static <O> O[] consolidateArray(O[] first, O[] last) {
        if (first == null || first.length == 0) {
            return last != null ? last : first;
        }
        if (last == null || last.length == 0) {
            return first;
        }
        O[] array = Arrays.copyOf(first, first.length + last.length);
        System.arraycopy(last, 0, array, first.length, last.length);
        return array;
    }

    protected static Area consolidateArea(JsonObject obj, String memeber, JsonDeserializationContext ctx, Area parent) {
        Area in;
        if (obj.has(memeber)) {
            in = ctx.deserialize(obj.get(memeber), Area.class);
            if (in == null) {
                return parent;
            }
        } else {
            return parent;
        }
        if (parent == null) return in;
        String x = consolidateFunction(in.x, parent.x, "");
        String y = consolidateFunction(in.y, parent.y, "");
        String width = consolidateFunction(in.width, parent.width, "");
        String height = consolidateFunction(in.height, parent.height, "");
        Area a = new Area(x, y, width, height);
        return a;
    }
}
