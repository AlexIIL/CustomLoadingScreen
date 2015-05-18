package alexiil.mods.load.json;

import java.util.Map;

import net.minecraft.util.ResourceLocation;

import com.google.common.collect.ObjectArrays;

import alexiil.mods.load.BLSLog;
import alexiil.mods.load.baked.BakedConfigurable;
import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionBaker;

/** @param <C> The class that extends this. This is what it should consolidate down to.
 * @param <B> The class that this is baked to. */
public abstract class JsonConfigurable<C extends JsonConfigurable<C, B>, B extends BakedConfigurable> {
    /** This is what its parent is. The definition of parent defines on what subclass this is of JsonConfigurable, see
     * each subclass for details. */
    public final String parent;
    /** This is used for debugging and is initialised from ConfigManager.getAsT() */
    public transient ResourceLocation resourceLocation;
    /** Cache variable for the consolidated version of this */
    private transient C consolidated;

    public JsonConfigurable(String parent) {
        this.parent = parent;
    }

    /** Bakes this into something that can be ticked quickly. This is a potentially expensive function, so don't call
     * this more than you need to. (This does not cache, as this could potentially be called with a different map of
     * functions) */
    public final B bake(Map<String, BakedFunction<?>> functions) {
        if (resourceLocation == null)
            throw new NullPointerException();
        try {
            B b = getConsolidated().actuallyBake(functions);
            b.setOrigin(resourceLocation);
            return b;
        }
        catch (Throwable t) {
            BLSLog.warn(resourceLocation + " failed to bake!", t);
            return null;
        }
    }

    /** Bakes this object into something that will process quickly. Only call this on a consolidated object! (Use
     * {@link #bake(Map)} instead of this) */
    protected abstract B actuallyBake(Map<String, BakedFunction<?>> functions);

    /** ALWAYS call this as opposed to {@link #actuallyConsolidate()}, as this caches the result. */
    public final C getConsolidated() {
        if (resourceLocation == null)
            throw new NullPointerException();
        if (consolidated == null) {
            try {
                consolidated = actuallyConsolidate();
                if (consolidated.resourceLocation == null)
                    consolidated.resourceLocation = resourceLocation;
            }
            catch (Throwable t) {
                BLSLog.warn(resourceLocation + " failed to consolidate!", t);
            }
        }
        return consolidated;
    }

    /** This consolidates this object with its parent, overriding at all stages. This also causes the parent to
     * consolidate with ITS parent, etc. This returns a new object with all overriding elements squashed down, unless
     * this objects parent is null. As a result, consolidate() does nothing if this has already been consolidated.
     * Generally, this method should only be called during this classes bake() method. */
    protected abstract C actuallyConsolidate();

    // These are helper methods to make consolidation one-liners

    protected String consolidateFunction(String in, String parent, String defaultF) {
        if (in == null)
            if (parent == null)
                return defaultF;
            else
                return parent;
        else if (parent == null) {
            return in;
        }
        else
            return FunctionBaker.expandParents(in, parent);
    }

    /** This will override the parents version of the object if in is not null. If in is null and the parent is null,
     * then the default is returned. */
    protected <O> O overrideObject(O in, O parent, O defaultO) {
        if (in != null)
            return in;
        if (parent != null)
            return parent;
        return defaultO;
    }

    /** This will effectively combine the two arrays together. Specifically, if both of the arrays are null, null is
     * returned. If both of them are either null or empty (but one is not null) then the non-null one is returned. If
     * both of them are not empty, then a new array is returned with the first array occupying the first positions, and
     * and the second array occupying the last positions. */
    @SuppressWarnings("unchecked")
    protected <O> O[] consolidateArray(O[] first, O[] last) {
        if (first == null || first.length == 0) {
            if (last == null)
                return null;
            else if (first != null)
                return first;
            else
                return last;
        }
        if (last == null || last.length == 0)
            return first;
        return ObjectArrays.concat(first, last, (Class<O>) first.getClass().getComponentType());
    }

    /** This will override the parent array with the in array. For example, passing in = { "hi", null, "three" } and
     * parent = { "one", "two" } would return { "hi", "two", "three" } */
    protected <O> O[] overrideArray(O[] in, O[] parent) {
        if (in == null || in.length == 0)
            return parent;
        if (parent == null || parent.length == 0)
            return in;
        O[] array = ObjectArrays.newArray(in, Math.max(in.length, parent.length));
        for (int i = 0; i < array.length; i++) {
            if (i < in.length && i < parent.length)
                array[i] = overrideObject(in[i], parent[i], null);
            else if (i >= in.length)
                array[i] = parent[i];
            else
                array[i] = in[i];
        }
        return array;

    }

    protected Area consolidateArea(Area in, Area parent) {
        if (in == null)
            return parent;
        if (parent == null)
            return in;
        String x = consolidateFunction(in.x, parent.x, null);
        String y = consolidateFunction(in.y, parent.y, null);;
        String width = consolidateFunction(in.width, parent.width, null);;
        String height = consolidateFunction(in.height, parent.height, null);;
        Area a = new Area(x, y, width, height);
        return a;
    }
}
