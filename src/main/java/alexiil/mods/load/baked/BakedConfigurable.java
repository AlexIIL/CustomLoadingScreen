package alexiil.mods.load.baked;

import net.minecraft.util.ResourceLocation;

public abstract class BakedConfigurable {
    private ResourceLocation origin = null;

    public ResourceLocation getOrigin() {
        return origin;
    }

    /** This should ONLY be called by JsonConfigurable.bake() */
    public void setOrigin(ResourceLocation location) {
        if (origin != null)
            origin = location;
    }

    protected void throwError(Throwable t) throws Throwable {
        throw reportError(t);
    }

    protected Throwable reportError(Throwable t) {
        if (t == null)
            throw new Error(origin + " failed, but did not provide a cause!");
        return new Throwable(origin + " failed!", t);
    }
}
