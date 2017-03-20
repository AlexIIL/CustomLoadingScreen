package alexiil.mc.mod.load.json.subtypes;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import alexiil.mc.mod.load.baked.render.BakedTextRenderStatic;
import alexiil.mc.mod.load.json.Area;
import alexiil.mc.mod.load.json.EPosition;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableString;

public class JsonRenderText extends JsonRenderPositioned {
    public final String text;

    public JsonRenderText(JsonRenderText parent, JsonObject json, JsonDeserializationContext context) {
        super(parent, json, context);
        text = overrideObject(json, "text", context, String.class, parent == null ? null : parent.text, "_missing_text_");
    }

    @Override
    protected BakedTextRenderStatic actuallyBake(FunctionContext context) throws InvalidExpressionException {
        NodeVariableString varText = context.putVariableString("text");
        NodeVariableLong varWidth = context.putVariableLong("elemWidth");
        NodeVariableLong varHeight = context.putVariableLong("elemHeight");

        INodeString textFunc = GenericExpressionCompiler.compileExpressionString(text, context);
        INodeDouble xFunc = GenericExpressionCompiler.compileExpressionDouble(position.x, context);
        INodeDouble yFunc = GenericExpressionCompiler.compileExpressionDouble(position.y, context);
        INodeLong colourFunc = GenericExpressionCompiler.compileExpressionLong(colour, context);
        return new BakedTextRenderStatic(varText, varWidth, varHeight, xFunc, yFunc, colourFunc, image, textFunc);
    }
}
