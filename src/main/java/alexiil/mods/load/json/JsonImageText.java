package alexiil.mods.load.json;

import java.util.Map;

import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.baked.render.BakedTextRenderStatic;

public class JsonImageText extends JsonImage {
    public JsonImageText(String resource, EPosition positionType, EPosition offsetPos, Area texture, Area position, String colour, String text) {
        super("", resource, positionType, offsetPos, texture, position, colour, text, null);
    }

    @Override
    protected JsonImageText actuallyConsolidate() {
        return this;
    }

    @Override
    protected BakedTextRenderStatic actuallyBake(Map<String, IBakedFunction<?>> functions) {
        return null;
    }
}
