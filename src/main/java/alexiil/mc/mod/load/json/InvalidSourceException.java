package alexiil.mc.mod.load.json;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.expression.InvalidExpressionException;

public class InvalidSourceException extends InvalidExpressionException {
    public InvalidSourceException(String message) {
        super(message);
    }

    public InvalidSourceException(String message, Throwable cause) {
        super(message);
        initCause(cause);
    }

    public InvalidSourceException(ResourceLocation loc, String src, Throwable cause) {
        this(loc + ":\n" + src, cause);
    }
}
