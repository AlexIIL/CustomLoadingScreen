package alexiil.mods.load.json;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import alexiil.mods.load.baked.BakedFactory;
import alexiil.mods.load.baked.BakedRenderingPart;
import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionBaker;

public class JsonFactory extends JsonConfigurable<JsonFactory, BakedFactory> {
    public final String shouldCreate, shouldDestroy, toCreate;

    public JsonFactory(String shouldCreate, String shouldDestroy, String toCreate) {
        super("");
        this.shouldCreate = shouldCreate;
        this.shouldDestroy = shouldDestroy;
        this.toCreate = toCreate;
    }

    @Override
    public BakedFactory actuallyBake(Map<String, BakedFunction<?>> functions) {
        BakedFunction<Boolean> shouldCreate = FunctionBaker.bakeFunctionBoolean(this.shouldCreate, functions);
        BakedFunction<Boolean> shouldDestroy = FunctionBaker.bakeFunctionBoolean(this.shouldDestroy, functions);
        JsonRenderingPart jrp = ConfigManager.getAsRenderingPart(toCreate).getConsolidated();
        BakedRenderingPart component = jrp.bake(functions);
        return new BakedFactory(shouldCreate, shouldDestroy, component);
    }

    @Override
    protected JsonFactory actuallyConsolidate() {
        if (StringUtils.isEmpty(parent))
            return this;
        JsonFactory parent = ConfigManager.getAsFactory(this.parent).getConsolidated();
        if (parent instanceof JsonFactoryStatus) {
            return new JsonFactoryStatus(shouldDestroy, toCreate);
        }
        String create = consolidateFunction(shouldCreate, parent.shouldCreate, "false");
        String destroy = consolidateFunction(shouldDestroy, parent.shouldDestroy, "false");
        String part = overrideObject(toCreate, parent.toCreate, "");
        return new JsonFactory(create, destroy, part);
    }
}
