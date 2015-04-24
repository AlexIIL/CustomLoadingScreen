package alexiil.mods.load.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import alexiil.mods.load.baked.BakedInstruction;
import alexiil.mods.load.baked.BakedRender;
import alexiil.mods.load.baked.BakedRenderingPart;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.IBakedFunction;

public class JsonRenderingPart {
    public final ImageRender element;
    public final JsonInstruction[] instructions;
    public final String shouldRender;

    public JsonRenderingPart(ImageRender image, JsonInstruction[] openGlInstructions, String shouldRender) {
        this.element = image;
        this.instructions = openGlInstructions;
        this.shouldRender = shouldRender;
    }

    public BakedRenderingPart bake(Map<String, IBakedFunction<?>> functions) {
        List<BakedInstruction> args = new ArrayList<BakedInstruction>();

        args.addAll(element.bakeInstructions(functions));

        for (JsonInstruction insn : instructions) {
            args.add(insn.bake(functions));
        }

        BakedInstruction[] instructions = args.toArray(new BakedInstruction[args.size()]);
        BakedRender actualRender = element.bake(functions);
        IBakedFunction<Boolean> shouldRenderFunc = FunctionBaker.bakeFunctionBoolean(shouldRender, functions);

        return new BakedRenderingPart(instructions, actualRender, shouldRenderFunc);
    }
}
