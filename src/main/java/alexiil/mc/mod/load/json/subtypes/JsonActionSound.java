package alexiil.mc.mod.load.json.subtypes;

import alexiil.mc.mod.load.baked.BakedAction;
import alexiil.mc.mod.load.baked.action.ActionSound;
import alexiil.mc.mod.load.json.JsonAction;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;
import buildcraft.lib.expression.api.IExpressionNode.INodeString;
import buildcraft.lib.expression.api.InvalidExpressionException;

public class JsonActionSound extends JsonAction {
    public final String sound;
    public final String repeat;

    public JsonActionSound(String conditionStart, String conditionEnd, String sound, String repeat) {
        super(conditionStart, conditionEnd);
        this.sound = sound;
        this.repeat = repeat;
    }

    @Override
    protected BakedAction actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        INodeBoolean start = GenericExpressionCompiler.compileExpressionBoolean(conditionStart, functions);
        INodeBoolean end = GenericExpressionCompiler.compileExpressionBoolean(conditionStart, functions);
        INodeString _sound = GenericExpressionCompiler.compileExpressionString(sound, functions);
        INodeBoolean _repeat = GenericExpressionCompiler.compileExpressionBoolean(repeat, functions);
        return new ActionSound(start, end, _sound, _repeat);
    }
}
