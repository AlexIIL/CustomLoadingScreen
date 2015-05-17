package alexiil.mods.load.baked.func.var;

import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.ICompilableFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedVariableSeconds extends BakedFunction<Double> implements ICompilableFunction<Double> {
    @Override
    public Double call(RenderingStatus status, Object... args) throws FunctionException {
        return status.getSeconds();
    }
}
