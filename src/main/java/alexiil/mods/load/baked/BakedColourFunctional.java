package alexiil.mods.load.baked;

import org.lwjgl.opengl.GL11;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedColourFunctional extends BakedInstruction {
    private final IBakedFunction<Double> red, green, blue, alpha;

    public BakedColourFunctional(IBakedFunction<Double> red, IBakedFunction<Double> green, IBakedFunction<Double> blue, IBakedFunction<Double> alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    @Override
    public void render(RenderingStatus status) throws FunctionException {
        GL11.glColor4d(red.call(status), green.call(status), blue.call(status), alpha.call(status));
    }
}
