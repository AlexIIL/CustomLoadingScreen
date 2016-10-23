package alexiil.mc.mod.load.json.subtypes;

import net.minecraft.util.ResourceLocation;

import alexiil.mc.mod.load.baked.BakedAction;
import alexiil.mc.mod.load.baked.action.ActionSound;
import alexiil.mc.mod.load.expression.FunctionContext;
import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.InvalidExpressionException;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeString;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableBoolean;
import alexiil.mc.mod.load.json.JsonAction;

public class JsonActionSound extends JsonAction {
    public JsonActionSound(ResourceLocation loc, String conditionStart, String conditionEnd, String[] arguments) {
        super("", conditionStart, conditionEnd, arguments);
        this.resourceLocation = loc;
    }

    @Override
    protected BakedAction actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        INodeBoolean start = GenericExpressionCompiler.compileExpressionBoolean(conditionStart, functions).derive(null);
        INodeBoolean end = GenericExpressionCompiler.compileExpressionBoolean(conditionStart, functions).derive(null);
        INodeString sound = GenericExpressionCompiler.compileExpressionString(arguments[0], functions).derive(null);
        INodeBoolean repeat;
        if (arguments.length > 1) {
            repeat = GenericExpressionCompiler.compileExpressionBoolean(arguments[1], functions).derive(null);
        } else {
            repeat = NodeImmutableBoolean.FALSE;
        }
        return new ActionSound(start, end, sound, repeat);
    }

    @Override
    protected JsonAction actuallyConsolidate() {
        return this;
    }
}
