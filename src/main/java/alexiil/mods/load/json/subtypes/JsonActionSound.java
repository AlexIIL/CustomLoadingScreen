package alexiil.mods.load.json.subtypes;

import java.util.Map;

import net.minecraft.util.ResourceLocation;

import alexiil.mods.load.baked.BakedAction;
import alexiil.mods.load.baked.action.ActionSound;
import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.var.BakedFunctionConstant;
import alexiil.mods.load.json.JsonAction;

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
