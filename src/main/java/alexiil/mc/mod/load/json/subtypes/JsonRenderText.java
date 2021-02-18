package alexiil.mc.mod.load.json.subtypes;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import alexiil.mc.mod.load.baked.render.BakedTextRenderStatic;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeObject;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeVariableDouble;
import buildcraft.lib.expression.node.value.NodeVariableObject;

public class JsonRenderText extends JsonRenderPositioned {
    public final String text;
    public final String scale;

    public JsonRenderText(JsonRenderText parent, JsonObject json, JsonDeserializationContext context) {
        super(parent, json, context);
        text = overrideObject(json, "text", context, String.class, parent == null ? null : parent.text, null);
        scale = overrideObject(json, "scale", context, String.class, parent == null ? null : parent.scale, null);
    }

    @Override
    protected BakedTextRenderStatic actuallyBake(FunctionContext context) throws InvalidExpressionException {
        NodeVariableObject<String> varText = context.putVariableString("text");
        NodeVariableDouble varWidth = context.putVariableDouble("elem_width");
        NodeVariableDouble varHeight = context.putVariableDouble("elem_height");

        ensureExists(text, "text");
        ensureExists(position, "position");
        ensureExists(position.x, "position.x");
        ensureExists(position.y, "position.y");
        ensureExists(colour, "colour");

        INodeObject<String> textFunc = GenericExpressionCompiler.compileExpressionObject(String.class, text, context);
        INodeDouble scaleFunc = scale == null ? NodeConstantDouble.ONE : GenericExpressionCompiler.compileExpressionDouble(scale, context);
        INodeDouble xFunc = GenericExpressionCompiler.compileExpressionDouble(position.x, context);
        INodeDouble yFunc = GenericExpressionCompiler.compileExpressionDouble(position.y, context);
        INodeLong colourFunc = GenericExpressionCompiler.compileExpressionLong(colour, context);
        return new BakedTextRenderStatic(varText, varWidth, varHeight, scaleFunc, xFunc, yFunc, colourFunc, image, textFunc);
    }
}
