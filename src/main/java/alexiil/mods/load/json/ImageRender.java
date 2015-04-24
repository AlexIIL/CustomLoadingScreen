package alexiil.mods.load.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import alexiil.mods.load.baked.BakedColourSimple;
import alexiil.mods.load.baked.BakedInstruction;
import alexiil.mods.load.baked.BakedPositionFunctional;
import alexiil.mods.load.baked.BakedRender;
import alexiil.mods.load.baked.BakedTextRenderStatic;
import alexiil.mods.load.baked.func.FunctionBaker;
import alexiil.mods.load.baked.func.IBakedFunction;
import alexiil.mods.load.render.BakedImageRender;

public class ImageRender {
    public final String resourceLocation;
    public final EPosition positionType;// Baked
    public final EPosition offsetPos;// Baked
    public final EType type;
    public final Area texture;
    public final Area position;// Baked
    public final String colour;// Baked
    public final String text;
    public final boolean animated;

    public ImageRender(String resource, EPosition positionType, EPosition offsetPos, EType type, Area texture, Area position, String colour,
            String text) {
        this.resourceLocation = resource;
        this.positionType = positionType;
        this.offsetPos = offsetPos;
        this.type = type;
        this.texture = texture;
        this.position = position;
        this.colour = colour;
        this.text = text;
        this.animated = false;
    }

    public ImageRender(String resourceLocation, EPosition positionType, EType type, Area texture, Area position, String colour, String text) {
        this(resourceLocation, positionType, positionType, type, texture, position, colour, text);
    }

    public ImageRender(String resourceLocation, EPosition positionType, EType type, Area texture, Area position) {
        this(resourceLocation, positionType, positionType, type, texture, position, null, null);
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

    public BakedRender bake(Map<String, IBakedFunction<?>> functions) {
        switch (type) {
            case DYNAMIC_PERCENTAGE: {
                String width = "percentage * (" + position.width + ")";
                String uWidth = "percentage * (" + texture.width + ")";
                return imageRender(position.x, position.y, width, position.height, texture.x, texture.y, uWidth, texture.height, functions);
            }
            case DYNAMIC_TEXT_PERCENTAGE: {
                return textRender("(percentage * 100) integer + '%'", position.x, position.y, colour, functions);
            }
            case DYNAMIC_TEXT_STATUS: {
                return textRender("status", position.x, position.y, colour, functions);
            }
            case STATIC: {
                if (animated) {
                    // TODO: work out animation
                    return null;
                }
                else {
                    return imageRender(position.x, position.y, position.width, position.height, texture.x, texture.y, texture.width, texture.height,
                        functions);
                }
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
            Map<String, IBakedFunction<?>> functions) {
        IBakedFunction<Double> xFunc = FunctionBaker.bakeFunctionDouble(x, functions);
        IBakedFunction<Double> yFunc = FunctionBaker.bakeFunctionDouble(y, functions);
        IBakedFunction<Double> widthFunc = FunctionBaker.bakeFunctionDouble(width, functions);
        IBakedFunction<Double> heightFunc = FunctionBaker.bakeFunctionDouble(height, functions);

        IBakedFunction<Double> uFunc = FunctionBaker.bakeFunctionDouble(u, functions);
        IBakedFunction<Double> vFunc = FunctionBaker.bakeFunctionDouble(v, functions);
        IBakedFunction<Double> uWidthFunc = FunctionBaker.bakeFunctionDouble(uWidth, functions);
        IBakedFunction<Double> vHeightFunc = FunctionBaker.bakeFunctionDouble(vHeight, functions);
        return new BakedImageRender(resourceLocation, xFunc, yFunc, widthFunc, heightFunc, uFunc, uWidthFunc, vFunc, vHeightFunc);
    }

    private BakedTextRenderStatic textRender(String text, String x, String y, String colour, Map<String, IBakedFunction<?>> functions) {
        IBakedFunction<String> textFunc = FunctionBaker.bakeFunctionString(text, functions);
        IBakedFunction<Double> xFunc = FunctionBaker.bakeFunctionDouble(x, functions);
        IBakedFunction<Double> yFunc = FunctionBaker.bakeFunctionDouble(y, functions);
        IBakedFunction<Double> colourFunc = FunctionBaker.bakeFunctionDouble(colour);
        return new BakedTextRenderStatic(textFunc, xFunc, yFunc, colourFunc, resourceLocation);
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
        }

        list.add(new BakedPositionFunctional(FunctionBaker.bakeFunctionDouble(x, functions), FunctionBaker.bakeFunctionDouble(y, functions),
            FunctionBaker.bakeFunctionDouble("0")));

        return list;
    }
}
