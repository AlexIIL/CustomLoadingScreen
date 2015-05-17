package alexiil.mods.load.json;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionBaker;

public class JsonFunction extends JsonConfigurable<JsonFunction, BakedFunction<?>> {
    // TODO: rethink functions to allow for arguments
    public final String name, function;
    public final String[] arguments;

    public JsonFunction(String name, String function, String[] arguments) {
        super("");
        this.name = name;
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    protected BakedFunction<?> actuallyBake(Map<String, BakedFunction<?>> functions) {
        return FunctionBaker.bakeFunction(function, functions, arguments);
    }

    @Override
    protected JsonFunction actuallyConsolidate() {
        if (StringUtils.isEmpty(parent))
            return this;

        JsonFunction func = ConfigManager.getAsFunction(parent);
        if (func == null)
            return this;

        func = func.getConsolidated();

        // Valid Functions (extend the one above it)
        // 1+2 TICK
        // super * 2 TICK -done via replacing "super" with "(<SUPER_FUNCTION>)"
        // (VAR=[number]) number / super NOPE- need arguments
        // super(50)

        // String function = consolidateFunction(this.function, func.function, "");
        String function = this.function.replace("super", func.name);
        // String[] args = consolidateArray(this.arguments, func.arguments);

        return new JsonFunction(name, function, arguments);
    }
}
