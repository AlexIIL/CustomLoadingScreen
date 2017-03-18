package alexiil.mc.mod.load.json;

import org.apache.commons.lang3.StringUtils;

import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionBaker;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InvalidExpressionException;

// FIXME: This doesn;t work this way any more!
public class JsonFunction extends JsonConfigurable<JsonFunction, BakedFunction<?>> {
    public final String name, function;
    public final String[] arguments;

    public JsonFunction(String name, String function, String[] arguments) {
        super("");
        this.name = name;
        this.function = function;
        this.arguments = arguments;
    }

    @Override
    protected BakedFunction<?> actuallyBake(FunctionContext functions) throws InvalidExpressionException {

        String start = "{";

        return FunctionBaker.bakeFunction(function, functions, arguments);
    }

    @Override
    protected JsonFunction actuallyConsolidate() {
        if (StringUtils.isEmpty(parent)) return this;

        JsonFunction func = ConfigManager.getAsFunction(parent);
        if (func == null) return this;

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
