package alexiil.mods.load.json;

import java.util.Map;

import alexiil.mods.load.baked.BakedColourFunctional;
import alexiil.mods.load.baked.BakedInstruction;
import alexiil.mods.load.baked.BakedPositionFunctional;
import alexiil.mods.load.baked.BakedRotationFunctional;
import alexiil.mods.load.baked.BakedScaleFunctional;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.IBakedFunction;

public class JsonInstruction {
    public final String function;
    public final String[] arguments;

    public JsonInstruction(String func, String[] args) {
        this.function = func;
        this.arguments = args;
    }

    public BakedInstruction bake(Map<String, IBakedFunction<?>> functions) {
        if (function.equalsIgnoreCase("rotate")) {
            IBakedFunction<Double> angle = FunctionBaker.bakeFunctionDouble(arguments[0], functions);
            IBakedFunction<Double> x = FunctionBaker.bakeFunctionDouble(arguments[1], functions);
            IBakedFunction<Double> y = FunctionBaker.bakeFunctionDouble(arguments[2], functions);
            IBakedFunction<Double> z = FunctionBaker.bakeFunctionDouble(arguments[3], functions);
            return new BakedRotationFunctional(angle, x, y, z);
        }
        else if (function.equalsIgnoreCase("scale")) {
            IBakedFunction<Double> x = FunctionBaker.bakeFunctionDouble(arguments[0], functions);
            IBakedFunction<Double> y = FunctionBaker.bakeFunctionDouble(arguments[1], functions);
            IBakedFunction<Double> z;
            if (arguments.length == 3)
                z = FunctionBaker.bakeFunctionDouble("1");
            else
                z = FunctionBaker.bakeFunctionDouble(arguments[2], functions);
            return new BakedScaleFunctional(x, y, z);
        }
        else if (function.equalsIgnoreCase("colour")) {
            IBakedFunction<Double> r = FunctionBaker.bakeFunctionDouble(arguments[0], functions);
            IBakedFunction<Double> g = FunctionBaker.bakeFunctionDouble(arguments[1], functions);
            IBakedFunction<Double> b = FunctionBaker.bakeFunctionDouble(arguments[2], functions);
            IBakedFunction<Double> alpha;
            if (arguments.length == 3)
                alpha = FunctionBaker.bakeFunctionDouble("1");
            else
                alpha = FunctionBaker.bakeFunctionDouble(arguments[3], functions);
            return new BakedColourFunctional(r, g, b, alpha);
        }
        else if (function.equalsIgnoreCase("position")) {
            IBakedFunction<Double> x = FunctionBaker.bakeFunctionDouble(arguments[0], functions);
            IBakedFunction<Double> y = FunctionBaker.bakeFunctionDouble(arguments[1], functions);
            IBakedFunction<Double> z;
            if (arguments.length == 2)
                z = FunctionBaker.bakeFunctionDouble("0");
            else
                z = FunctionBaker.bakeFunctionDouble(arguments[2], functions);
            return new BakedPositionFunctional(x, y, z);
        }
        return null;
    }
}
