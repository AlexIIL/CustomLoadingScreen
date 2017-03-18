package alexiil.mc.mod.load.json.subtypes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.insn.BakedInstruction;
import alexiil.mc.mod.load.baked.insn.BakedPositionFunctional;
import alexiil.mc.mod.load.baked.render.BakedTextRenderStatic;
import alexiil.mc.mod.load.json.Area;
import alexiil.mc.mod.load.json.EPosition;
import alexiil.mc.mod.load.json.JsonImage;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.api.IExpressionNode.INodeLong;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantDouble;
import buildcraft.lib.expression.node.value.NodeVariableLong;
import buildcraft.lib.expression.node.value.NodeVariableString;

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
        NodeVariableString varText = functions.putVariableString("text");
        NodeVariableLong varWidth = functions.putVariableLong("textwidth");
        NodeVariableLong varHeight = functions.putVariableLong("textheight");

        INodeString textFunc = GenericExpressionCompiler.compileExpressionString(text, functions);
        INodeDouble xFunc = GenericExpressionCompiler.compileExpressionDouble(position.x, functions);
        INodeDouble yFunc = GenericExpressionCompiler.compileExpressionDouble(position.y, functions);
        INodeLong colourFunc = GenericExpressionCompiler.compileExpressionLong(colour, functions);
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

        INodeDouble expX = GenericExpressionCompiler.compileExpressionDouble(x, functions);
        INodeDouble expY = GenericExpressionCompiler.compileExpressionDouble(y, functions);
        INodeDouble expZ = NodeConstantDouble.ZERO;
        list.add(new BakedPositionFunctional(expX, expY, expZ));

        return list;
    }
}
