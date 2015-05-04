package alexiil.mods.load.json;

import net.minecraft.util.ResourceLocation;

public class JsonFunction {
    public final String name, function;
    public transient ResourceLocation resourceLocation;

    public JsonFunction(String name, String function) {
        this.name = name;
        this.function = function;
    }
}
