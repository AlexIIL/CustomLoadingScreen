package alexiil.mc.mod.load.baked.func.var;

import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.baked.func.ICompilableFunction;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedVariableScreenHeight extends BakedFunction<Double>implements ICompilableFunction<Double> {
    @Override
    public Double call(RenderingStatus status, Object... args) throws FunctionException {
        return (double) status.getScreenHeight();
    }
}
