package alexiil.mc.mod.load.json.subtypes;

import java.util.Collections;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.insn.BakedInstruction;
import alexiil.mc.mod.load.baked.render.BakedPanoramaRender;
import alexiil.mc.mod.load.json.JsonImage;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;

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
        INodeDouble angle = GenericExpressionCompiler.compileExpressionDouble("time * 40", functions);
        return new BakedPanoramaRender(angle, image);
    }

    @Override
    public List<BakedInstruction> bakeInstructions(FunctionContext functions) {
        return Collections.emptyList();
    }
}
