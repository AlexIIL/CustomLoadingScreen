package alexiil.mc.mod.load.baked;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

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
        if (location != null) origin = location;
        if (src != null) rawText = src;
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

    public void preLoad(MinecraftDisplayerRenderer renderer) {
        
    }
}
