package alexiil.mc.mod.load.baked.render;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

public class BakedArea {
    public final INodeDouble x, y, width, height;

    public BakedArea(String x, String y, String width, String height, FunctionContext context) throws InvalidExpressionException {
        this.x = GenericExpressionCompiler.compileExpressionDouble(x, context);
        this.y = GenericExpressionCompiler.compileExpressionDouble(y, context);
        this.width = GenericExpressionCompiler.compileExpressionDouble(width, context);
        this.height = GenericExpressionCompiler.compileExpressionDouble(height, context);
    }

    public BakedArea(INodeDouble x, INodeDouble y, INodeDouble width, INodeDouble height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }
}
