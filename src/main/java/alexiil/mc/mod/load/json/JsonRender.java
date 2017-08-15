package alexiil.mc.mod.load.json;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.baked.insn.BakedInsn;
import alexiil.mc.mod.load.json.JsonInsn.JsonInsnColourTogether;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.api.InvalidExpressionException;

public abstract class JsonRender extends JsonConfigurable<JsonRender, BakedRender> {
    public final String image;
    public final String colour;

    public JsonRender(String image, String colour) {
        this.image = image;
        this.colour = colour;
    }

    public JsonRender(JsonRender parent, JsonObject json, JsonDeserializationContext context) {
        this.image = overrideObject(json, "image", context, String.class, parent == null ? null : parent.image, "missingno");
        this.colour = overrideObject(json, "colour", context, String.class, parent == null ? null : parent.colour, "0xFFFFFFFF");
    }

    public List<BakedInsn> bakeInstructions(FunctionContext context) throws InvalidExpressionException {
        List<BakedInsn> list = new ArrayList<>();
        JsonInsnColourTogether insn = new JsonInsnColourTogether(colour);
        insn.setLocation(resourceLocation);
        list.add(insn.bake(context));
        return list;
    }
}
