package alexiil.mc.mod.load.baked.insn;

import org.lwjgl.opengl.GL11;

import alexiil.mc.mod.load.expression.FunctionContext;
import alexiil.mc.mod.load.expression.InvalidExpressionException;
import alexiil.mc.mod.load.json.EPosition;

public class BakedPositionSimple extends BakedInstruction {
    private final double x, y;

    public static BakedInstruction bake(EPosition general, EPosition specific, String x, String y, FunctionContext functions) throws InvalidExpressionException {
        // if (general != EPosition.TOP_LEFT || specific != EPosition.TOP_LEFT || isInvalid(x) || isInvalid(y)) {
        // String xFunc = general.getFunctionX(specific.getFunctionX(x));
        // String yFunc = general.getFunctionY(specific.getFunctionY(y));
        // return BakedPositionFunctional.bake(xFunc, yFunc, functions);
        return BakedPositionFunctional.bake(x, y, functions);
        // }
        // return new BakedPositionSimple(Double.parseDouble(x), Double.parseDouble(y));
    }

    private static boolean isInvalid(String d) {
        try {
            Double.parseDouble(d);
            return false;
        } catch (Throwable ignored) {
            return true;
        }
    }

    public BakedPositionSimple(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render() {
        GL11.glTranslated(x, y, 0);
    }
}
