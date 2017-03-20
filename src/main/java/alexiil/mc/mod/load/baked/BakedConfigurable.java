package alexiil.mc.mod.load.baked;

import net.minecraft.util.ResourceLocation;

public abstract class BakedConfigurable {
    private ResourceLocation origin = null;
    private String rawText;

    public ResourceLocation getOrigin() {
        return origin;
    }

    public String getRawText() {
        return rawText;
    }

    public final void setOrigin(ResourceLocation location, String src) {
        if (origin != null) origin = location;
        if (rawText != null) rawText = src;
    }

    protected void throwError(Throwable cause) throws Error {
        throw reportError(cause);
    }

    protected Error reportError(Throwable cause) {
        if (cause == null) {
            throw new Error(origin + " failed, but did not provide a cause!\n" + rawText);
        }
        return new Error(origin + " failed!\n" + rawText, cause);
    }
}
