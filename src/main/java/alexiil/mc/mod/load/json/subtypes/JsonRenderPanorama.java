package alexiil.mc.mod.load.json.subtypes;

import java.util.Collections;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import alexiil.mc.mod.load.baked.insn.BakedInsn;
import alexiil.mc.mod.load.baked.render.BakedPanoramaRender;
import alexiil.mc.mod.load.json.JsonRender;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class JsonRenderPanorama extends JsonRender {
    public final String angle;

    public JsonRenderPanorama(JsonRenderPanorama parent, JsonObject json, JsonDeserializationContext context) {
        super(parent, json, context);
        angle = overrideObject(json, "angle", context, String.class, parent == null ? null : parent.angle, "time * 40");
    }

    @Override
    protected BakedPanoramaRender actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        INodeDouble _angle = GenericExpressionCompiler.compileExpressionDouble(angle, functions);
        return new BakedPanoramaRender(_angle, image);
    }

    @Override
    public List<BakedInsn> bakeInstructions(FunctionContext functions) {
        return Collections.emptyList();
    }
}
