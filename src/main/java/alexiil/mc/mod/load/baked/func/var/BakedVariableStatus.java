package alexiil.mc.mod.load.baked.func.var;

import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionException;
import alexiil.mc.mod.load.baked.func.ICompilableFunction;
import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedVariableStatus extends BakedFunction<String>implements ICompilableFunction<String> {
    @Override
    public String call(RenderingStatus status, Object... args) throws FunctionException {
        return status.progressState.getCurrentProgress().status;
    }
}
