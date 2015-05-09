package alexiil.mods.load.json;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import alexiil.mods.load.baked.BakedConfigurable;
import alexiil.mods.load.baked.func.IBakedFunction;

public class JsonFunction extends JsonConfigurable<JsonFunction, BakedConfigurable> {
    public final String name, function;

    public JsonFunction(String name, String function) {
        super("");
        this.name = name;
        this.function = function;
    }

    @Override
    protected BakedConfigurable actuallyBake(Map<String, IBakedFunction<?>> functions) {
        return null;// NO-OP
    }

    @Override
    protected JsonFunction actuallyConsolidate() {
        if (StringUtils.isEmpty(parent))
            return this;

        JsonFunction func = ConfigManager.getAsFunction(parent);
        if (func == null)
            return this;

        func = func.getConsolidated();

        String function = consolidateFunction(this.function, func.function, "");

        return new JsonFunction(name, function);
    }
}
