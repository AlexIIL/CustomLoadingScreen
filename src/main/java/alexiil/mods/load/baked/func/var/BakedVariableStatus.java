package alexiil.mods.load.baked.func.var;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.ICompilableFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedVariableStatus implements ICompilableFunction<String> {
    @Override
    public String call(RenderingStatus status) throws FunctionException {
        return status.progressState.getCurrentProgress().status;
    }
}
