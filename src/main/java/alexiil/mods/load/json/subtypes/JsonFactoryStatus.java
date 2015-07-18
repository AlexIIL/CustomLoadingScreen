package alexiil.mods.load.json.subtypes;

import java.util.Map;

import alexiil.mods.load.baked.BakedRenderingPart;
import alexiil.mods.load.baked.factory.BakedFactoryStatus;
import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.json.ConfigManager;
import alexiil.mods.load.json.JsonFactory;
import alexiil.mods.load.json.JsonRenderingPart;

public class JsonFactoryStatus extends JsonFactory {
    public JsonFactoryStatus(String shouldDestroy, String toCreate) {
        // None of the arguments matter
        super("false", shouldDestroy, toCreate);
    }

    public JsonFactoryStatus() {
        // None of the arguments matter
        super("false", "false", null);
    }

    @Override
    public BakedFactoryStatus actuallyBake(Map<String, BakedFunction<?>> functions) {
        BakedFunction<Boolean> shouldDestroy = FunctionBaker.bakeFunctionBoolean(this.shouldDestroy, functions);

        JsonRenderingPart jrp = ConfigManager.getAsRenderingPart(toCreate);
        if (jrp == null) {

            return null;
        }
        jrp = jrp.getConsolidated();
        BakedRenderingPart component = jrp.bake(functions);
        return new BakedFactoryStatus(shouldDestroy, component);
    }

    /** This is overridden just so that the correct type is returned, with no casting needed. Note that the children
     * will have to consolidate down to this class, otherwise it won't work properly. */
    @Override
    protected JsonFactoryStatus actuallyConsolidate() {
        return this;
    }
}
