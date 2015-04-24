package alexiil.mods.load.baked.func.stack;

import java.util.Deque;

import alexiil.mods.load.render.RenderingStatus;

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
