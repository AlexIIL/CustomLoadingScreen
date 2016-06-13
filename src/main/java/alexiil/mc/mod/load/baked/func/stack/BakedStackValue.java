package alexiil.mc.mod.load.baked.func.stack;

import java.util.Deque;

import alexiil.mc.mod.load.render.RenderingStatus;

public class BakedStackValue<T> extends BakedStackFunction {
    private final T value;

    public BakedStackValue(T value) {
        this.value = value;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void doOperation(Deque stack, RenderingStatus status) {
        stack.push(value);
    }

    @Override
    public String toString() {
        return "Value [] -> [(" + value.getClass().getName() + ")], value = \"" + value + "\" ";
    }
}
