package alexiil.mods.load.baked.func.stack;

import java.util.Deque;

import alexiil.mods.load.baked.func.BakedPostFixFunction;
import alexiil.mods.load.baked.func.BakedPostFixFunction.IBakedStackFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedStackDebug implements IBakedStackFunction {
    private final int index;
    private final BakedPostFixFunction<?> bakedPostFixFunction;

    public BakedStackDebug(int index, BakedPostFixFunction<?> bakedPostFixFunction) {
        this.index = index;
        this.bakedPostFixFunction = bakedPostFixFunction;
    }

    @Override
    public void doOperation(Deque<?> stack, RenderingStatus status) throws StackFunctionException {
        System.out.println("  -" + StackFunctionException.getMessage(bakedPostFixFunction, status, stack, index).replace("\n", "\n  "));
    }

    @Override
    public String toString() {
        return "Debugger  [] -> []";
    }
}
