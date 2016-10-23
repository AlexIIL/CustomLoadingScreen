package alexiil.mc.mod.load.json.subtypes;

import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.insn.BakedInstruction;
import alexiil.mc.mod.load.baked.render.BakedPanoramaRender;
import alexiil.mc.mod.load.expression.FunctionContext;
import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.InvalidExpressionException;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.json.JsonImage;

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
    protected BakedPanoramaRender actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        INodeDouble angle = GenericExpressionCompiler.compileExpressionDouble("seconds * 40", functions).derive(null);
        return new BakedPanoramaRender(angle, image);
    }

    @Override
    public List<BakedInstruction> bakeInstructions(FunctionContext functions) {
        return Collections.emptyList();
    }
}
