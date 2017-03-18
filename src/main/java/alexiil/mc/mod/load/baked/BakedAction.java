package alexiil.mc.mod.load.baked;

import alexiil.mc.mod.load.render.MinecraftDisplayerRenderer;

import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

public abstract class BakedAction extends BakedTickable {
    protected final INodeBoolean conditionStart, conditionEnd;
    private boolean started = false;

    public BakedAction(INodeBoolean conditionStart, INodeBoolean conditionEnd) {
        this.conditionStart = conditionStart;
        this.conditionEnd = conditionEnd;
    }

    @Override
    public void tick(MinecraftDisplayerRenderer renderer) {
        if (!started && conditionStart.evaluate()) {
            started = true;
            start();
        } else if (started) {
            tick();
        }
        if (started && conditionEnd.evaluate()) {
            started = false;
            end();
        }
    }

    /** Called the first time conditionStart resolves to true */
    protected abstract void start();

    /** Called once per tick after start() has been called, and before stop() has been called */
    protected abstract void tick();

    /** Called whenever conditionEnd resolves to true, provided that start() has already been called */
    protected abstract void end();
}
