package alexiil.mc.mod.load.json.subtypes;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedAction;
import alexiil.mc.mod.load.baked.action.ActionSound;
import alexiil.mc.mod.load.json.JsonAction;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.node.value.NodeConstantBoolean;

public class JsonActionSound extends JsonAction {
    public JsonActionSound(ResourceLocation loc, String conditionStart, String conditionEnd, String[] arguments) {
        super("", conditionStart, conditionEnd, arguments);
        this.resourceLocation = loc;
    }

    @Override
    protected BakedAction actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        INodeBoolean start = GenericExpressionCompiler.compileExpressionBoolean(conditionStart, functions);
        INodeBoolean end = GenericExpressionCompiler.compileExpressionBoolean(conditionStart, functions);
        INodeString sound = GenericExpressionCompiler.compileExpressionString(arguments[0], functions);
        INodeBoolean repeat;
        if (arguments.length > 1) {
            repeat = GenericExpressionCompiler.compileExpressionBoolean(arguments[1], functions);
        } else {
            repeat = NodeConstantBoolean.FALSE;
        }
        return new ActionSound(start, end, sound, repeat);
    }

    @Override
    protected JsonAction actuallyConsolidate() {
        return this;
    }
}
