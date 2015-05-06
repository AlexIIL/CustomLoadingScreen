package alexiil.mods.load.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import alexiil.mods.load.BLSLog;
import alexiil.mods.load.baked.BakedRender;
import alexiil.mods.load.baked.BakedRenderingPart;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.baked.insn.BakedInstruction;

/** A rendering part is something that defines the meta about a particular ImageRender: so, OpenGL commands and whether
 * or not it should render at this time */

public class JsonRenderingPart extends JsonConfigurable<JsonRenderingPart, BakedRenderingPart> {
    public final String image;
    public final JsonInstruction[] instructions;
    public final String shouldRender;

    public JsonRenderingPart(String image, JsonInstruction[] openGlInstructions, String shouldRender, String parent) {
        super(parent);
        this.image = image;
        this.instructions = openGlInstructions;
        this.shouldRender = shouldRender;
    }

    @Override
    protected BakedRenderingPart actuallyBake(Map<String, IBakedFunction<?>> functions) {
        List<BakedInstruction> args = new ArrayList<BakedInstruction>();

        JsonImage element = ConfigManager.getAsImage(image);
        element = element.getConsolidated();

        if (element.resourceLocation == null) {
            BLSLog.warn("An elements resource location (child of " + this.resourceLocation + ") was null!");
            element.resourceLocation = this.resourceLocation;
        }

        args.addAll(element.bakeInstructions(functions));

        if (instructions != null) {
            for (JsonInstruction insn : instructions) {
                args.add(insn.bake(functions));
            }
        }

        BakedInstruction[] instructions = args.toArray(new BakedInstruction[args.size()]);
        BakedRender actualRender = element.bake(functions);
        IBakedFunction<Boolean> shouldRenderFunc = FunctionBaker.bakeFunctionBoolean(shouldRender, functions);

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

        // Consolidate the parent, just in case it has its own parents
        parent = parent.getConsolidated();

        // Image: Just override the parents image if this image is not null
        String image = overrideObject(this.image, parent.image, null);

        // Instructions: Append the parents instructions to this array
        JsonInstruction[] insns = consolidateArray(this.instructions, parent.instructions);

        // ShouldRender: Expand any "super" tokens (if they exist), or use the parents one if this is null
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
