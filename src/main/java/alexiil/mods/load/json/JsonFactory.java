package alexiil.mods.load.json;

import java.util.Map;

import alexiil.mods.load.baked.BakedFactory;
import alexiil.mods.load.baked.func.IBakedFunction;

public class JsonFactory {
    public enum EFactory {

    }

    public final EFactory type;
    public final String shouldCreate, shouldDestroy;
    public final JsonRenderingPart toCreate;

    public JsonFactory(EFactory type, String shouldCreate, String shouldDestroy, JsonRenderingPart toCreate) {
        this.type = type;
        this.shouldCreate = shouldCreate;
        this.shouldDestroy = shouldDestroy;
        this.toCreate = toCreate;
    }

    public BakedFactory bake(Map<String, IBakedFunction<?>> functions) {
        return null;
    }
}
