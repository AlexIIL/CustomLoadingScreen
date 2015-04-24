package alexiil.mods.load.baked;

import org.lwjgl.opengl.GL11;

import alexiil.mods.load.baked.func.FunctionException;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.render.RenderingStatus;

public class BakedRotationFunctional extends BakedInstruction {
    private final IBakedFunction<Double> angle, x, y, z;

    public BakedRotationFunctional(IBakedFunction<Double> angle, IBakedFunction<Double> x, IBakedFunction<Double> y, IBakedFunction<Double> z) {
        this.angle = angle;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void render(RenderingStatus status) throws FunctionException {
        double angle = this.angle.call(status);
        double x = this.x.call(status);
        double y = this.y.call(status);
        double z = this.z.call(status);
        GL11.glRotated(angle, x, y, z);
    }
}
