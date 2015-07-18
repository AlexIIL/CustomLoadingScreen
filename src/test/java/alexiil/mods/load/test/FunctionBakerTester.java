package alexiil.mods.load.test;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.Maps;

import org.junit.Test;

import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.var.BakedFunctionConstant;
import alexiil.mods.load.render.RenderingStatus;

public class FunctionBakerTester {
    private static final RenderingStatus status = new RenderingStatus(1000, 500);
    private static final Map<String, BakedFunction<?>> emptyMap = Collections.emptyMap();

    static {
        FunctionBaker.step = true;
    }

    @Test
    public void testBasics() {
        // I COULD change all these to be in separate functions... except that thats really long :/
        bakeAndCallDouble("0", 0);
        bakeAndCallDouble("-1", -1);
        bakeAndCallDouble("0+1", 1);
        bakeAndCallDouble("   0   +    1    ", 1);
        bakeAndCallDouble("3-1", 2);
        bakeAndCallDouble("1+1+1", 3);
        bakeAndCallDouble("1+2-1", 2);
        bakeAndCallDouble("1-2+1", 0);
        bakeAndCallDouble("2^5", 32);
        bakeAndCallDouble("1+2^5*3", 97);
        bakeAndCallDouble("(49)^(1/2)-(2*3)", 1);
        bakeAndCallDouble("2*(-3)", -6);
    }

    @Test
    public void testFunctions() {
        Map<String, BakedFunction<?>> functions = Maps.newHashMap();
        functions.put("one", new BakedFunctionConstant<Double>(1.0));
        bakeAndCallDouble("one", 1, functions);
        bakeAndCallDouble("oNe", 1, functions);

        BakedFunction<Double> same = FunctionBaker.bakeFunction("{0}", functions, new String[1]);
        functions.put("same", same);
        bakeAndCallDouble("same(0)", 0, functions);
        bakeAndCallDouble("same(one)", 1, functions);
        bakeAndCallDouble("same(2^5)", 32, functions);

        BakedFunction<Double> powerTwo = FunctionBaker.bakeFunction("2^{0}", functions, new String[1]);
        functions.put("powertwo", powerTwo);
        bakeAndCallDouble("powerTwo(5)", 32, functions);
        bakeAndCallDouble("powertwo(6)", 64, functions);

        BakedFunction<Double> subtract = FunctionBaker.bakeFunction("{0}-{1}", functions, new String[2]);
        functions.put("subtract", subtract);
        bakeAndCallDouble("subtract(3, 1)", 2, functions);
        bakeAndCallDouble("subtract(1, 3)", -2, functions);
    }

    private void bakeAndCallDouble(String function, double def) {
        bakeAndCallDouble(function, def, emptyMap);
    }

    private void bakeAndCallDouble(String function, double expected, Map<String, BakedFunction<?>> functions) {
        System.out.println("Testing " + function + ", expecting " + expected);
        try {
            BakedFunction<Double> func = FunctionBaker.bakeFunctionDouble(function, functions);
            double got = func.call(status);
            assertEquals(expected, got, Math.ulp(expected) * 2);
        } catch (FunctionException fe) {
            fail(fe.getMessage());
        }
    }
}
