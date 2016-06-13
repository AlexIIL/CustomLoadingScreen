package alexiil.mc.mod.load.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.func.BakedFunction;
import alexiil.mc.mod.load.baked.func.FunctionBaker;
import alexiil.mc.mod.load.baked.insn.BakedInstruction;

/** A rendering part is something that defines the meta about a particular ImageRender: so, OpenGL commands and whether
 * or not it should render at this time */
public class JsonRenderingPart extends JsonConfigurable<JsonRenderingPart, BakedRenderingPart> {
    public final String image;
    public final String[] instructions;
    public final String shouldRender;

    public JsonRenderingPart(String image, String[] openGlInstructions, String shouldRender, String parent) {
        super(parent);
        this.image = image;
        this.instructions = openGlInstructions;
        this.shouldRender = shouldRender;
    }

    @Override
    protected BakedRenderingPart actuallyBake(Map<String, BakedFunction<?>> functions) {
        List<BakedInstruction> args = new ArrayList<BakedInstruction>();

        JsonImage element = ConfigManager.getAsImage(image).getConsolidated();

        args.addAll(element.bakeInstructions(functions));

        if (instructions != null) {
            for (String insn : instructions) {
                JsonInstruction ji = ConfigManager.getAsInsn(insn).getConsolidated();
                args.add(ji.bake(functions));
            }
        }

        BakedInstruction[] instructions = args.toArray(new BakedInstruction[args.size()]);
        BakedRender actualRender = element.bake(functions);
        BakedFunction<Boolean> shouldRenderFunc = FunctionBaker.bakeFunctionBoolean(shouldRender == null ? "true" : shouldRender, functions);

        return new BakedRenderingPart(instructions, actualRender, shouldRenderFunc);
    }

    @Override
    protected JsonRenderingPart actuallyConsolidate() {
        // If this has already been consolidated or doesn't have any parents, don't try to consolidate
        if (StringUtils.isEmpty(parent))
            return this;

        JsonRenderingPart parent = ConfigManager.getAsRenderingPart(this.parent);
        // If the parent wasn't null, but the parent did not exist, then just return this as the error has already been
        // logged by the ConfigManager
        if (parent == null)
            return this;

        parent = parent.getConsolidated();
        String image = overrideObject(this.image, parent.image, null);
        String[] insns = consolidateArray(this.instructions, parent.instructions);
        String render = consolidateFunction(shouldRender, parent.shouldRender, "true");

        // Initialise the new object with all the details, and set its resource location to this one (as it will act in
        // exactly the same way once baked)
        JsonRenderingPart newOne = new JsonRenderingPart(image, insns, render, null);
        newOne.resourceLocation = resourceLocation;
        return newOne;
    }

    public JsonImage getImageRender() {
        JsonRenderingPart consolidated = getConsolidated();
        if (consolidated.image != null) {
            return ConfigManager.getAsImage(consolidated.image);
        }
        return null;
    }
}
