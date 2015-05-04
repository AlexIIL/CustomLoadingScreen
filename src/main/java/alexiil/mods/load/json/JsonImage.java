package alexiil.mods.load.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import alexiil.mods.load.baked.BakedRender;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.baked.insn.BakedColourSimple;
import alexiil.mods.load.baked.insn.BakedInstruction;
import alexiil.mods.load.baked.insn.BakedPositionFunctional;
import alexiil.mods.load.baked.render.BakedAnimatedRender;
import alexiil.mods.load.baked.render.BakedImageRender;
import alexiil.mods.load.baked.render.BakedPanoramaRender;
import alexiil.mods.load.baked.render.BakedTextRenderStatic;
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
        if (parent == null)
            return this;

        JsonImage parent = ConfigManager.getAsImage(this.parent);
        if (parent == null)
            return this;
        parent = parent.getConsolidated();

        String image = overrideObject(this.image, parent.image, null);
        EPosition positionType = overrideObject(this.positionType, parent.positionType, null);
        EPosition offsetPos = overrideObject(this.offsetPos, parent.positionType, null);
        Area texture = overrideObject(this.texture, parent.texture, null);
        Area position = overrideObject(this.position, parent.position, null);
        String colour = overrideObject(this.colour, parent.colour, "FFFFFF");
        String text = overrideObject(this.text, parent.text, null);
        String frame = overrideObject(this.frame, parent.frame, "0");

        if (parent instanceof JsonImageText) {
            return new JsonImageText(image, positionType, offsetPos, texture, position, colour, text);
        }

        return new JsonImage("", image, positionType, offsetPos, texture, position, colour, text, frame);
    }

    @Override
    protected BakedRender actuallyBake(Map<String, IBakedFunction<?>> functions) {
        switch (type) {
            case DYNAMIC_PERCENTAGE: {
                String width = "percentage * (" + position.width + ")";
                String uWidth = "percentage * (" + texture.width + ")";
                return imageRender(position.x, position.y, width, position.height, texture.x, texture.y, uWidth, texture.height, functions, "0");
            }
            case DYNAMIC_TEXT_PERCENTAGE: {
                return textRender("(percentage * 100) integer + '%'", position.x, position.y, colour, functions);
            }
            case DYNAMIC_TEXT_STATUS: {
                return textRender("status", position.x, position.y, colour, functions);
            }
            case DYNAMIC_PANORAMA: {
                return panoramaRender(functions);
            }
            case STATIC: {
                return imageRender(position.x, position.y, position.width, position.height, texture.x, texture.y, texture.width, texture.height,
                    functions, frame);
            }
            case STATIC_TEXT: {
                return textRender(text, position.x, position.y, colour, functions);
            }
            default:
                throw new Error("Blame whoever added a type to EType without editing ImageRender.bake()! (type = " + type + ")");
        }
        // throw new Error(type + " needs to be baked!");
    }

    private BakedImageRender imageRender(String x, String y, String width, String height, String u, String v, String uWidth, String vHeight,
            Map<String, IBakedFunction<?>> functions, String frame) {
        IBakedFunction<Double> xFunc = FunctionBaker.bakeFunctionDouble(x, functions);
        IBakedFunction<Double> yFunc = FunctionBaker.bakeFunctionDouble(y, functions);
        IBakedFunction<Double> widthFunc = FunctionBaker.bakeFunctionDouble(width, functions);
        IBakedFunction<Double> heightFunc = FunctionBaker.bakeFunctionDouble(height, functions);

        IBakedFunction<Double> uFunc = FunctionBaker.bakeFunctionDouble(u, functions);
        IBakedFunction<Double> vFunc = FunctionBaker.bakeFunctionDouble(v, functions);
        IBakedFunction<Double> uWidthFunc = FunctionBaker.bakeFunctionDouble(uWidth, functions);
        IBakedFunction<Double> vHeightFunc = FunctionBaker.bakeFunctionDouble(vHeight, functions);
        if (TextureAnimator.isAnimated(resourceLocation.toString())) {
            IBakedFunction<Double> frameFunc = FunctionBaker.bakeFunctionDouble(frame, functions);
            return new BakedAnimatedRender(image, xFunc, yFunc, widthFunc, heightFunc, uFunc, uWidthFunc, vFunc, vHeightFunc, frameFunc);
        }
        else
            return new BakedImageRender(image, xFunc, yFunc, widthFunc, heightFunc, uFunc, uWidthFunc, vFunc, vHeightFunc);
    }

    private BakedTextRenderStatic textRender(String text, String x, String y, String colour, Map<String, IBakedFunction<?>> functions) {
        IBakedFunction<String> textFunc = FunctionBaker.bakeFunctionString(text, functions);
        IBakedFunction<Double> xFunc = FunctionBaker.bakeFunctionDouble(x, functions);
        IBakedFunction<Double> yFunc = FunctionBaker.bakeFunctionDouble(y, functions);
        IBakedFunction<Double> colourFunc = FunctionBaker.bakeFunctionDouble(colour);
        return new BakedTextRenderStatic(textFunc, xFunc, yFunc, colourFunc, image);
    }

    private BakedPanoramaRender panoramaRender(Map<String, IBakedFunction<?>> functions) {
        IBakedFunction<Double> angle = FunctionBaker.bakeFunctionDouble(frame == null ? "seconds * 20" : frame, functions);
        return new BakedPanoramaRender(angle, image);
    }

    public List<BakedInstruction> bakeInstructions(Map<String, IBakedFunction<?>> functions) {
        List<BakedInstruction> list = new ArrayList<BakedInstruction>();
        if (getColour() != 0xFFFFFF && type != EType.STATIC_TEXT && type != EType.DYNAMIC_TEXT_PERCENTAGE && type != EType.DYNAMIC_TEXT_STATUS) {
            list.add(new BakedColourSimple(getRed(), getGreen(), getBlue(), 1));
        }

        String x = "";
        String y = "";

        switch (type) {
            case STATIC:
            case DYNAMIC_PERCENTAGE: {
                x = positionType.getFunctionX("screenWidth", offsetPos.getFunctionX(position.width, position.x));
                y = positionType.getFunctionY("screenHeight", offsetPos.getFunctionY(position.height, position.y));
                break;
            }
            case STATIC_TEXT:
            case DYNAMIC_TEXT_STATUS:
            case DYNAMIC_TEXT_PERCENTAGE: {
                x = positionType.getFunctionX("screenWidth", offsetPos.getFunctionX("('textWidth':variable)", position.x));
                y = positionType.getFunctionY("screenHeight", offsetPos.getFunctionY("('textHeight':variable)", position.y));
                break;
            }
            case DYNAMIC_PANORAMA:
                return list;
        }

        list.add(new BakedPositionFunctional(FunctionBaker.bakeFunctionDouble(x, functions), FunctionBaker.bakeFunctionDouble(y, functions),
            FunctionBaker.bakeFunctionDouble("0")));

        return list;
    }
}
