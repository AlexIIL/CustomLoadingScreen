package alexiil.mods.load.baked;

import java.util.Map;

import org.lwjgl.opengl.GL11;

import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.json.EPosition;
import alexiil.mods.load.render.RenderingStatus;

public class BakedPositionSimple extends BakedInstruction {
    private final double x, y;

    public static BakedInstruction bake(EPosition general, EPosition specific, String x, String y, Map<String, IBakedFunction<?>> functions) {
        if (general != EPosition.TOP_LEFT || specific != EPosition.TOP_LEFT || isInvalid(x) || isInvalid(y)) {
//            String xFunc = general.getFunctionX(specific.getFunctionX(x));
//            String yFunc = general.getFunctionY(specific.getFunctionY(y));
//            return BakedPositionFunctional.bake(xFunc, yFunc, functions);
        }
        return new BakedPositionSimple(Double.parseDouble(x), Double.parseDouble(y));
    }

    private static boolean isInvalid(String d) {
        try {
            Double.parseDouble(d);
            return false;
        }
        catch (Throwable ignored) {
            return true;
        }
    }

    public BakedPositionSimple(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(RenderingStatus status) {
        GL11.glTranslated(x, y, 0);
    }
}
