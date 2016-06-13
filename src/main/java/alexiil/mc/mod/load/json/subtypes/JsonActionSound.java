package alexiil.mc.mod.load.json.subtypes;

import java.util.Map;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedAction;
import alexiil.mc.mod.load.baked.action.ActionSound;
import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionBaker;
import alexiil.mc.mod.load.baked.func.var.BakedFunctionConstant;
import alexiil.mc.mod.load.json.JsonAction;

public class JsonActionSound extends JsonAction {
    public JsonActionSound(ResourceLocation loc, String conditionStart, String conditionEnd, String[] arguments) {
        super("", conditionStart, conditionEnd, arguments);
        this.resourceLocation = loc;
    }

    @Override
    protected BakedAction actuallyBake(Map<String, BakedFunction<?>> functions) {
        BakedFunction<Boolean> start = FunctionBaker.bakeFunctionBoolean(conditionStart, functions);
        BakedFunction<Boolean> end = FunctionBaker.bakeFunctionBoolean(conditionEnd, functions);
        BakedFunction<String> sound = FunctionBaker.bakeFunctionString(arguments[0], functions);
        BakedFunction<Boolean> repeat;
        if (arguments.length > 1)
            repeat = FunctionBaker.bakeFunctionBoolean(arguments[1], functions);
        else
            repeat = new BakedFunctionConstant<Boolean>(true);
        return new ActionSound(start, end, sound, repeat);
    }

    @Override
    protected JsonAction actuallyConsolidate() {
        return this;
    }
}
