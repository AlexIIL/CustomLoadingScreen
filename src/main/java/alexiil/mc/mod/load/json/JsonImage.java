package alexiil.mc.mod.load.json;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import alexiil.mc.mod.load.baked.BakedRender;
import alexiil.mc.mod.load.baked.insn.BakedColourSimple;
import alexiil.mc.mod.load.baked.insn.BakedInstruction;
import alexiil.mc.mod.load.baked.insn.BakedPositionFunctional;
import alexiil.mc.mod.load.baked.render.BakedAnimatedRender;
import alexiil.mc.mod.load.baked.render.BakedImageRender;
import alexiil.mc.mod.load.expression.FunctionContext;
import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.InvalidExpressionException;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableDouble;
import alexiil.mc.mod.load.json.subtypes.JsonImagePanorama;
import alexiil.mc.mod.load.json.subtypes.JsonImageText;
import alexiil.mc.mod.load.render.TextureAnimator;

public class JsonImage extends JsonConfigurable<JsonImage, BakedRender> {
    public final String image;
    public final EPosition positionType;
    public final EPosition offsetPos;
    public final Area texture;
    public final Area position;
    public final String colour;
    public final String text;
    public final String frame;

    public JsonImage(String parent, String image, EPosition positionType, EPosition offsetPos, Area texture, Area position, String colour, String text, String frame) {
        super(parent);
        this.image = image;
        this.positionType = positionType;
        this.offsetPos = offsetPos;
        this.texture = texture;
        this.position = position;
        this.colour = colour;
        this.text = text;
        this.frame = frame;
    }

    public int getColour() {
        if (colour == null) return 0xFFFFFF;
        else {
            try {
                return Integer.parseInt(colour, 16);
            } catch (NumberFormatException nfe) {
                return 0xFFFFFF;
            }
        }
    }

    private float getColourPart(int bitStart) {
        return ((getColour() >> bitStart) & 0xFF) / 256F;
    }

    public float getRed() {
        return getColourPart(16);
    }

    public float getGreen() {
        return getColourPart(8);
    }

    public float getBlue() {
        return getColourPart(0);
    }

    @Override
    protected JsonImage actuallyConsolidate() {
        if (StringUtils.isEmpty(parent)) return this;

        JsonImage parent = ConfigManager.getAsImage(this.parent);
        if (parent == null) return this;
        parent = parent.getConsolidated();

        String image = overrideObject(this.image, parent.image, null);
        EPosition positionType = overrideObject(this.positionType, parent.positionType, null);
        EPosition offsetPos = overrideObject(this.offsetPos, parent.positionType, null);
        Area texture = consolidateArea(this.texture, parent.texture);
        Area position = consolidateArea(this.position, parent.position);
        String colour = overrideObject(this.colour, parent.colour, "0xFFFFFF");
        String text = overrideObject(this.text, parent.text, null);
        String frame = overrideObject(this.frame, parent.frame, "0");

        if (parent instanceof JsonImageText) {
            return new JsonImageText(resourceLocation, image, positionType, offsetPos, texture, position, colour, text);
        }
        if (parent instanceof JsonImagePanorama) {
            return new JsonImagePanorama(resourceLocation, image);
        }

        return new JsonImage("", image, positionType, offsetPos, texture, position, colour, text, frame);
    }

    @Override
    protected BakedRender actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        INodeDouble xFunc = GenericExpressionCompiler.compileExpressionDouble(position.x, functions).derive(null);
        INodeDouble yFunc = GenericExpressionCompiler.compileExpressionDouble(position.y, functions).derive(null);
        INodeDouble widthFunc = GenericExpressionCompiler.compileExpressionDouble(position.width, functions).derive(null);
        INodeDouble heightFunc = GenericExpressionCompiler.compileExpressionDouble(position.height, functions).derive(null);

        INodeDouble uFunc = GenericExpressionCompiler.compileExpressionDouble(texture.x, functions).derive(null);
        INodeDouble vFunc = GenericExpressionCompiler.compileExpressionDouble(texture.y, functions).derive(null);
        INodeDouble uWidthFunc = GenericExpressionCompiler.compileExpressionDouble(texture.width, functions).derive(null);
        INodeDouble vHeightFunc = GenericExpressionCompiler.compileExpressionDouble(texture.height, functions).derive(null);
        if (TextureAnimator.isAnimated(resourceLocation.toString())) {
            INodeDouble frameFunc = GenericExpressionCompiler.compileExpressionDouble(frame, functions).derive(null);
            return new BakedAnimatedRender(image, xFunc, yFunc, widthFunc, heightFunc, uFunc, uWidthFunc, vFunc, vHeightFunc, frameFunc);
        } else {
            return new BakedImageRender(image, xFunc, yFunc, widthFunc, heightFunc, uFunc, uWidthFunc, vFunc, vHeightFunc);
        }
    }

    public List<BakedInstruction> bakeInstructions(FunctionContext functions) throws InvalidExpressionException {
        List<BakedInstruction> list = new ArrayList<>();
        if (getColour() != 0xFFFFFF) {
            list.add(new BakedColourSimple(getRed(), getGreen(), getBlue(), 1));
        }

        String x = positionType.getFunctionX("screenWidth", offsetPos.getFunctionX(position.width, "0"/* position.x */));
        String y = positionType.getFunctionY("screenHeight", offsetPos.getFunctionY(position.height, "0"/* position.y */));

        INodeDouble expX = GenericExpressionCompiler.compileExpressionDouble(x, functions).derive(null);
        INodeDouble expY = GenericExpressionCompiler.compileExpressionDouble(y, functions).derive(null);
        INodeDouble expZ = new NodeImmutableDouble(0);

        list.add(new BakedPositionFunctional(expX, expY, expZ));

        return list;
    }
}
