package alexiil.mc.mod.load.json.subtypes;

import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import alexiil.mc.mod.load.baked.insn.BakedInsn;
import alexiil.mc.mod.load.baked.insn.BakedTranslateFunctional;
import alexiil.mc.mod.load.json.Area;
import alexiil.mc.mod.load.json.EPosition;
import alexiil.mc.mod.load.json.JsonRender;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public abstract class JsonRenderPositioned extends JsonRender {
    public final EPosition positionType;
    public final EPosition offsetPos;
    public final Area position;

    public JsonRenderPositioned(JsonRenderPositioned parent, JsonObject json, JsonDeserializationContext context) {
        super(parent, json, context);
        positionType = overrideObject(
            json, "position_type", context, EPosition.class, parent == null ? null : parent.positionType,
            EPosition.CENTER
        );
        offsetPos = overrideObject(
            json, "offset_pos", context, EPosition.class, parent == null ? null : parent.offsetPos, EPosition.CENTER
        );
        position = consolidateArea(json, "position", context, parent == null ? null : parent.position);
    }

    @Override
    public List<BakedInsn> bakeInstructions(FunctionContext context) throws InvalidExpressionException {
        List<BakedInsn> list = super.bakeInstructions(context);
        String x = positionType.getPositionFunctionX("screen_width", offsetPos.getOffsetFunctionX("elem_width"));
        String y = positionType.getPositionFunctionY("screen_height", offsetPos.getOffsetFunctionY("elem_height"));

        INodeDouble expX = GenericExpressionCompiler.compileExpressionDouble(x, context);
        INodeDouble expY = GenericExpressionCompiler.compileExpressionDouble(y, context);
        INodeDouble expZ = NodeConstantDouble.ZERO;
        list.add(new BakedTranslateFunctional(expX, expY, expZ));
        return list;
    }
}
