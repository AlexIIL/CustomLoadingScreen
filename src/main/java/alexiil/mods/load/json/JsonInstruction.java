package alexiil.mods.load.json;

import java.util.Map;

import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.baked.insn.BakedColourFunctional;
import alexiil.mods.load.baked.insn.BakedInstruction;
import alexiil.mods.load.baked.insn.BakedPositionFunctional;
import alexiil.mods.load.baked.insn.BakedRotationFunctional;
import alexiil.mods.load.baked.insn.BakedScaleFunctional;

public class JsonInstruction extends JsonConfigurable<JsonInstruction, BakedInstruction> {
    public final String function;
    public final String[] arguments;

    public JsonInstruction(String func, String[] args) {
        super("");
        this.function = func;
        this.arguments = args;
    }

    // TODO: Convert JsonInstruction to use parents for rotation, scaling, colour and position
    public BakedInstruction actuallyBake(Map<String, IBakedFunction<?>> functions) {
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

    @Override
    protected JsonInstruction actuallyConsolidate() {
        // TODO Auto-generated method stub
        return null;
    }
}
