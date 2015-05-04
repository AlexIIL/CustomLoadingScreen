package alexiil.mods.load.json;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import alexiil.mods.load.baked.BakedFactory;
import alexiil.mods.load.baked.BakedRenderingPart;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.IBakedFunction;

public class JsonFactory extends JsonConfigurable<JsonFactory, BakedFactory> {
    public final String shouldCreate, shouldDestroy;
    public final JsonRenderingPart toCreate;

    public JsonFactory(String shouldCreate, String shouldDestroy, JsonRenderingPart toCreate) {
        super("");
        this.shouldCreate = shouldCreate;
        this.shouldDestroy = shouldDestroy;
        this.toCreate = toCreate;
    }

    @Override
    public BakedFactory actuallyBake(Map<String, IBakedFunction<?>> functions) {
        IBakedFunction<Boolean> shouldCreate = FunctionBaker.bakeFunctionBoolean(this.shouldCreate, functions);
        IBakedFunction<Boolean> shouldDestroy = FunctionBaker.bakeFunctionBoolean(this.shouldDestroy, functions);
        BakedRenderingPart component = toCreate == null ? null : toCreate.bake(functions);
        return new BakedFactory(shouldCreate, shouldDestroy, component);
    }

    @Override
    protected JsonFactory actuallyConsolidate() {
        if (StringUtils.isEmpty(parent))
            return this;
        JsonFactory fact = ConfigManager.getAsFactory(parent);
        JsonFactory parent = fact.getConsolidated();
        if (parent instanceof JsonFactoryStatus) {
            return new JsonFactoryStatus(shouldDestroy, toCreate);
        }
        String create = consolidateFunction(shouldCreate, parent.shouldCreate, "false");
        String destroy = consolidateFunction(shouldDestroy, parent.shouldDestroy, "false");
        JsonRenderingPart part = overrideObject(toCreate, parent.toCreate, null);
        return new JsonFactory(create, destroy, part);
    }
}
