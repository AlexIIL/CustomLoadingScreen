package alexiil.mods.load.json;

import java.util.Map;

import alexiil.mods.load.baked.BakedAction;
import alexiil.mods.load.baked.action.ActionSound;
import alexiil.mods.load.baked.func.BakedFunctionConstant;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.IBakedFunction;

public class JsonAction {
    public enum EType {
        SOUND;
    }

    public final EType type;
    public final String conditionStart;
    public final String conditionEnd;
    public final String[] arguments;

    public JsonAction(EType type, String conditionStart, String conditionEnd, String[] arguments) {
        this.type = type;
        this.conditionStart = conditionStart;
        this.conditionEnd = conditionEnd;
        this.arguments = arguments;
    }

    public BakedAction bake(Map<String, IBakedFunction<?>> functions) {
        switch (type) {
            case SOUND: {
                IBakedFunction<Boolean> start = FunctionBaker.bakeFunctionBoolean(conditionStart, functions);
                IBakedFunction<Boolean> end = FunctionBaker.bakeFunctionBoolean(conditionEnd, functions);
                IBakedFunction<String> sound = FunctionBaker.bakeFunctionString(arguments[0], functions);
                IBakedFunction<Boolean> repeat;
                if (arguments.length > 1)
                    repeat = FunctionBaker.bakeFunctionBoolean(arguments[1], functions);
                else
                    repeat = new BakedFunctionConstant<Boolean>(true);
                return new ActionSound(start, end, sound, repeat);
            }
        }
        return null;
    }
}
