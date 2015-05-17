package alexiil.mods.load.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import alexiil.mods.load.baked.BakedRender;
import alexiil.mods.load.baked.func.BakedFunction;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.insn.BakedColourSimple;
import alexiil.mods.load.baked.insn.BakedInstruction;
import alexiil.mods.load.baked.insn.BakedPositionFunctional;
import alexiil.mods.load.baked.render.BakedAnimatedRender;
import alexiil.mods.load.baked.render.BakedImageRender;
import alexiil.mods.load.render.TextureAnimator;

public class JsonImage extends JsonConfigurable<JsonImage, BakedRender> {
    public final String image;
    public final EPosition positionType;
    public final EPosition offsetPos;
    @Deprecated
    public EType type;
    public final Area texture;
    public final Area position;
    public final String colour;
    public final String text;
    public final String frame;

    public JsonImage(String parent, String image, EPosition positionType, EPosition offsetPos, Area texture, Area position, String colour,
            String text, String frame) {
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
        if (colour == null)
            return 0xFFFFFF;
        else {
            try {
                return Integer.parseInt(colour, 16);
            }
            catch (NumberFormatException nfe) {
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
        if (StringUtils.isEmpty(parent))
            return this;

        JsonImage parent = ConfigManager.getAsImage(this.parent);
        if (parent == null)
            return this;
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
    protected BakedRender actuallyBake(Map<String, BakedFunction<?>> functions) {
        BakedFunction<Double> xFunc = FunctionBaker.bakeFunctionDouble(position.x, functions);
        BakedFunction<Double> yFunc = FunctionBaker.bakeFunctionDouble(position.y, functions);
        BakedFunction<Double> widthFunc = FunctionBaker.bakeFunctionDouble(position.width, functions);
        BakedFunction<Double> heightFunc = FunctionBaker.bakeFunctionDouble(position.height, functions);

        BakedFunction<Double> uFunc = FunctionBaker.bakeFunctionDouble(texture.x, functions);
        BakedFunction<Double> vFunc = FunctionBaker.bakeFunctionDouble(texture.y, functions);
        BakedFunction<Double> uWidthFunc = FunctionBaker.bakeFunctionDouble(texture.width, functions);
        BakedFunction<Double> vHeightFunc = FunctionBaker.bakeFunctionDouble(texture.height, functions);
        if (TextureAnimator.isAnimated(resourceLocation.toString())) {
            BakedFunction<Double> frameFunc = FunctionBaker.bakeFunctionDouble(frame, functions);
            return new BakedAnimatedRender(image, xFunc, yFunc, widthFunc, heightFunc, uFunc, uWidthFunc, vFunc, vHeightFunc, frameFunc);
        }
        else
            return new BakedImageRender(image, xFunc, yFunc, widthFunc, heightFunc, uFunc, uWidthFunc, vFunc, vHeightFunc);

        // switch (type) {
        // case DYNAMIC_PERCENTAGE: {
        // String width = "percentage * (" + position.width + ")";
        // String uWidth = "percentage * (" + texture.width + ")";
        // return imageRender(position.x, position.y, width, position.height, texture.x, texture.y, uWidth,
        // texture.height, functions, "0");
        // }
        // case DYNAMIC_TEXT_PERCENTAGE: {
        // return textRender("(percentage * 100) integer + '%'", position.x, position.y, colour, functions);
        // }
        // case DYNAMIC_TEXT_STATUS: {
        // return textRender("status", position.x, position.y, colour, functions);
        // }
        // case DYNAMIC_PANORAMA: {
        // return panoramaRender(functions);
        // }
        // case STATIC: {
        // return imageRender(position.x, position.y, position.width, position.height, texture.x, texture.y,
        // texture.width, texture.height,
        // functions, frame);
        // }
        // case STATIC_TEXT: {
        // return textRender(text, position.x, position.y, colour, functions);
        // }
        // default:
        // throw new Error("Blame whoever added a type to EType without editing ImageRender.bake()! (type = " + type +
        // ")");
        // }
        // throw new Error(type + " needs to be baked!");
    }

    // private BakedPanoramaRender panoramaRender(Map<String, IBakedFunction<?>> functions) {
    // IBakedFunction<Double> angle = FunctionBaker.bakeFunctionDouble(frame == null ? "seconds * 20" : frame,
    // functions);
    // return new BakedPanoramaRender(angle, image);
    // }

    public List<BakedInstruction> bakeInstructions(Map<String, BakedFunction<?>> functions) {
        List<BakedInstruction> list = new ArrayList<BakedInstruction>();
        if (getColour() != 0xFFFFFF) {
            list.add(new BakedColourSimple(getRed(), getGreen(), getBlue(), 1));
        }

        String x = positionType.getFunctionX("screenWidth", offsetPos.getFunctionX(position.width, "0"/* position.x */));
        String y = positionType.getFunctionY("screenHeight", offsetPos.getFunctionY(position.height, "0"/* position.y */));

        list.add(new BakedPositionFunctional(FunctionBaker.bakeFunctionDouble(x, functions), FunctionBaker.bakeFunctionDouble(y, functions),
            FunctionBaker.bakeFunctionDouble("0")));

        return list;
    }
}
