package alexiil.mc.mod.load.json;

import alexiil.mc.mod.load.baked.render.BakedArea;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InvalidExpressionException;

public class Area {
    public final String x, y, width, height;

    public Area(double x, double y, double width, double height) {
        this.x = Double.toString(x);
        this.y = Double.toString(y);
        this.width = Double.toString(width);
        this.height = Double.toString(height);
    }

    public Area(String x, String y, String width, String height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "Area [x=" + x + ", y=" + y + ", width=" + width + ", height=" + height + "]";
    }

    public BakedArea bake(FunctionContext context) throws InvalidExpressionException {
        return new BakedArea(x, y, width, height, context);
    }
}
