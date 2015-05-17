package alexiil.mods.load.baked.func.var;

import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.ICompilableFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedVariableStatus extends BakedFunction<String> implements ICompilableFunction<String> {
    @Override
    public String call(RenderingStatus status, Object... args) throws FunctionException {
        return status.progressState.getCurrentProgress().status;
    }
}
