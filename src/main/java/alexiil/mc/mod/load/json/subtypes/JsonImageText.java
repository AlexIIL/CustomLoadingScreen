package alexiil.mc.mod.load.json.subtypes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.insn.BakedInstruction;
import alexiil.mc.mod.load.baked.insn.BakedPositionFunctional;
import alexiil.mc.mod.load.baked.render.BakedTextRenderStatic;
import alexiil.mc.mod.load.expression.FunctionContext;
import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.InvalidExpressionException;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeLong;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableDouble;
import alexiil.mc.mod.load.expression.node.value.NodeMutableLong;
import alexiil.mc.mod.load.expression.node.value.NodeMutableString;
import alexiil.mc.mod.load.json.Area;
import alexiil.mc.mod.load.json.EPosition;
import alexiil.mc.mod.load.json.JsonImage;

public class JsonImageText extends JsonImage {
    public JsonImageText(ResourceLocation resourceLocation, String image, EPosition positionType, EPosition offsetPos, Area texture, Area position, String colour, String text) {
        super("", image, positionType, offsetPos, texture, position, colour, text, null);
        this.resourceLocation = resourceLocation;
    }

    @Override
    protected JsonImageText actuallyConsolidate() {
        return this;
    }

    @Override
    protected BakedTextRenderStatic actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        NodeMutableString varText = functions.getOrAddString("text");
        NodeMutableLong varWidth = functions.getOrAddLong("textwidth");
        NodeMutableLong varHeight = functions.getOrAddLong("textheight");

        INodeString textFunc = GenericExpressionCompiler.compileExpressionString(text, functions).derive(null);
        INodeDouble xFunc = GenericExpressionCompiler.compileExpressionDouble(position.x, functions).derive(null);
        INodeDouble yFunc = GenericExpressionCompiler.compileExpressionDouble(position.y, functions).derive(null);
        INodeLong colourFunc = GenericExpressionCompiler.compileExpressionLong(colour, functions).derive(null);
        return new BakedTextRenderStatic(varText, varWidth, varHeight, xFunc, yFunc, colourFunc, image, textFunc);
    }

    @Override
    public List<BakedInstruction> bakeInstructions(FunctionContext functions) throws InvalidExpressionException {
        List<BakedInstruction> list = new ArrayList<>();
        // if (getColour() != 0xFFFFFF) {
        // list.add(new BakedColourSimple(getRed(), getGreen(), getBlue(), 1));
        // }

        String x = positionType.getFunctionX("screenWidth", offsetPos.getFunctionX("textWidth", "0"));
        String y = positionType.getFunctionY("screenHeight", offsetPos.getFunctionY("textHeight", "0"));

        INodeDouble expX = GenericExpressionCompiler.compileExpressionDouble(x, functions).derive(null);
        INodeDouble expY = GenericExpressionCompiler.compileExpressionDouble(y, functions).derive(null);
        INodeDouble expZ = new NodeImmutableDouble(0);
        list.add(new BakedPositionFunctional(expX, expY, expZ));

        return list;
    }
}
