package alexiil.mods.load.json.subtypes;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.util.ResourceLocation;

import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.insn.BakedInstruction;
import alexiil.mods.load.baked.render.BakedPanoramaRender;
import alexiil.mods.load.json.JsonImage;

public class JsonImagePanorama extends JsonImage {

    public JsonImagePanorama(ResourceLocation resourceLocation, String image) {
        super("", image, null, null, null, null, null, null, null);
        this.resourceLocation = resourceLocation;
    }

    @Override
    protected JsonImagePanorama actuallyConsolidate() {
        return this;
    }

    @Override
    protected BakedPanoramaRender actuallyBake(Map<String, BakedFunction<?>> functions) {
        BakedFunction<Double> angle = FunctionBaker.bakeFunctionDouble("seconds * 40", functions);
        return new BakedPanoramaRender(angle, image);
    }

    @Override
    public List<BakedInstruction> bakeInstructions(Map<String, BakedFunction<?>> functions) {
        return Collections.emptyList();
    }
}
