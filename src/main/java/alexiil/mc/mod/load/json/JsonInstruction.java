package alexiil.mc.mod.load.json;

import alexiil.mc.mod.load.baked.insn.*;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeDouble;
import buildcraft.lib.expression.node.value.NodeConstantDouble;

public class JsonInstruction extends JsonConfigurable<JsonInstruction, BakedInstruction> {
    public final String function;
    public final String[] arguments;

    public JsonInstruction(String func, String[] args) {
        super("");
        this.function = func;
        this.arguments = args;
    }

    // TODO: Convert JsonInstruction to use parents for rotation, scaling, colour and position
    @Override
    public BakedInstruction actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        if (function.equalsIgnoreCase("rotate")) {
            INodeDouble angle = GenericExpressionCompiler.compileExpressionDouble(arguments[0], functions);
            INodeDouble x = GenericExpressionCompiler.compileExpressionDouble(arguments[1], functions);
            INodeDouble y = GenericExpressionCompiler.compileExpressionDouble(arguments[2], functions);
            INodeDouble z = GenericExpressionCompiler.compileExpressionDouble(arguments[3], functions);
            return new BakedRotationFunctional(angle, x, y, z);
        } else if (function.equalsIgnoreCase("scale")) {
            INodeDouble x = GenericExpressionCompiler.compileExpressionDouble(arguments[0], functions);
            INodeDouble y = GenericExpressionCompiler.compileExpressionDouble(arguments[1], functions);
            INodeDouble z;
            if (arguments.length == 3) z = new NodeConstantDouble(1);
            else z = GenericExpressionCompiler.compileExpressionDouble(arguments[2], functions);
            return new BakedScaleFunctional(x, y, z);
        } else if (function.equalsIgnoreCase("colour")) {
            INodeDouble r = GenericExpressionCompiler.compileExpressionDouble(arguments[0], functions);
            INodeDouble g = GenericExpressionCompiler.compileExpressionDouble(arguments[1], functions);
            INodeDouble b = GenericExpressionCompiler.compileExpressionDouble(arguments[2], functions);
            INodeDouble alpha;
            if (arguments.length == 3) alpha = new NodeConstantDouble(1);
            else alpha = GenericExpressionCompiler.compileExpressionDouble(arguments[3], functions);
            return new BakedColourFunctional(r, g, b, alpha);
        } else if (function.equalsIgnoreCase("position")) {
            INodeDouble x = GenericExpressionCompiler.compileExpressionDouble(arguments[0], functions);
            INodeDouble y = GenericExpressionCompiler.compileExpressionDouble(arguments[1], functions);
            INodeDouble z;
            if (arguments.length == 2) z = new NodeConstantDouble(0);
            else z = GenericExpressionCompiler.compileExpressionDouble(arguments[2], functions);
            return new BakedPositionFunctional(x, y, z);
        }
        return null;
    }

    @Override
    protected JsonInstruction actuallyConsolidate() {
        // TODO Auto-generated method stub
        return this;
    }
}
