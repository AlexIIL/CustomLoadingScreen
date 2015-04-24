package alexiil.mods.load.baked;

import org.lwjgl.opengl.GL11;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedPositionFunctional extends BakedInstruction {
    private final IBakedFunction<Double> x, y, z;

    public BakedPositionFunctional(IBakedFunction<Double> x, IBakedFunction<Double> y, IBakedFunction<Double> z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void render(RenderingStatus status) throws FunctionException {
        GL11.glTranslated(x.call(status), y.call(status), z.call(status));
    }
}
