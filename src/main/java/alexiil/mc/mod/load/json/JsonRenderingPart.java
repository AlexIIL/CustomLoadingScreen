package alexiil.mc.mod.load.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.insn.BakedInsn;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

/** A rendering part is something that defines the meta about a particular ImageRender: so, OpenGL commands and whether
 * or not it should render at this time */
public class JsonRenderingPart extends JsonConfigurable<JsonRenderingPart, BakedRenderingPart> {
    public final JsonRender image;
    public final JsonInsn[] instructions;
    public final String shouldRender;

    public JsonRenderingPart(JsonRender image, JsonInsn[] instructions, String shouldRender) {
        this.image = image;
        this.instructions = instructions;
        this.shouldRender = shouldRender;
    }

    public JsonRenderingPart(JsonRenderingPart parent, JsonObject json, JsonDeserializationContext context) {
        if (json.has("image")) {
            image = context.deserialize(json.get("image"), JsonRender.class);
        } else {
            image = parent == null ? null : parent.image;
        }
        JsonInsn[] insns;
        if (json.has("instructions")) {
            insns = context.deserialize(json.get("instructions"), JsonInsn[].class);
        } else {
            insns = new JsonInsn[0];
        }
        this.instructions = consolidateArray(parent == null ? null : parent.instructions, insns);
        this.shouldRender = consolidateFunction(json, "should_render", context, parent == null ? null : parent.shouldRender, "true");
    }

    @Override
    public void setLocation(ResourceLocation location) {
        super.setLocation(location);
        location = this.resourceLocation;
        image.setLocation(location);
        for (JsonInsn insn : instructions) {
            insn.setLocation(location);
        }
    }

    @Override
    protected BakedRenderingPart actuallyBake(FunctionContext context) throws InvalidExpressionException {
        List<BakedInsn> args = new ArrayList<>();

        JsonRender element = image;

        context = new FunctionContext(context);

        BakedRender actualRender = element.bake(context);
        args.addAll(element.bakeInstructions(context));

        if (instructions != null) {
            for (JsonInsn insn : instructions) {
                args.add(insn.bake(context));
            }
        }

        BakedInsn[] instructions = args.toArray(new BakedInsn[args.size()]);

        INodeBoolean shouldRenderFunc;
        // = FunctionBaker.bakeFunctionBoolean(shouldRender == null ? "true" : shouldRender, functions);
        if (shouldRender == null) {
            shouldRenderFunc = NodeConstantBoolean.TRUE;
        } else {
            shouldRenderFunc = GenericExpressionCompiler.compileExpressionBoolean(shouldRender, context);
        }

        return new BakedRenderingPart(instructions, actualRender, shouldRenderFunc);
    }

    public JsonRender getImageRender() {
        return image;
    }
}
