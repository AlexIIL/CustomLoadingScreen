package alexiil.mc.mod.load.json.subtypes;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.baked.render.BakedArea;
import alexiil.mc.mod.load.baked.render.BakedSlideshowRender;
import alexiil.mc.mod.load.render.TextureLoader;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.InvalidExpressionException;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeVariableDouble;

public class JsonRenderSlideshow extends JsonRenderImage {

    public JsonRenderSlideshow(JsonRenderSlideshow parent, JsonObject json, JsonDeserializationContext context) {
        super(parent, json, context);
    }

    @Override
    protected BakedRender actuallyBake(FunctionContext context) throws InvalidExpressionException {
        NodeVariableDouble varWidth = context.putVariableDouble("elem_width");
        NodeVariableDouble varHeight = context.putVariableDouble("elem_height");
        INodeLong _colour = GenericExpressionCompiler.compileExpressionLong(colour, context);
        BakedArea pos = position.bake(context);
        BakedArea tex;
        if (texture == null) {
            NodeConstantDouble zero = NodeConstantDouble.ZERO;
            NodeConstantDouble one = NodeConstantDouble.ONE;
            tex = new BakedArea(zero, zero, one, one);
        } else {
            tex = texture.bake(context);
        }

        // Find available textures
        List<ResourceLocation> images = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            String l = image.replace("#", Integer.toString(i));

            ResourceLocation r = new ResourceLocation(l);
            try {
                InputStream stream = TextureLoader.openResourceStream(r);
                if (stream == null) {
                    if (i == 0) {
                        continue;
                    } else {
                        break;
                    }
                }
                stream.close();
            } catch (FileNotFoundException fnfe) {
                if (i == 0) {
                    continue;
                } else {
                    break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
            images.add(r);
        }

        if (images.isEmpty()) {
            images.add(new ResourceLocation("missingno"));
        }

        FunctionContext ctx2 = new FunctionContext("frame", context);
        ctx2.putConstantLong("frame_count", images.size());
        INodeDouble frameNode = GenericExpressionCompiler.compileExpressionDouble(frame, ctx2);

        return new BakedSlideshowRender(
            varWidth, varHeight, _colour, frameNode, images.toArray(new ResourceLocation[0]), pos, tex
        );
    }
}
