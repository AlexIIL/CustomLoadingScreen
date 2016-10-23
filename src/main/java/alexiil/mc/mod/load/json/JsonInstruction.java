package alexiil.mc.mod.load.json;

import alexiil.mc.mod.load.baked.insn.*;
import alexiil.mc.mod.load.expression.FunctionContext;
import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.InvalidExpressionException;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeDouble;
import alexiil.mc.mod.load.expression.node.value.NodeImmutableDouble;

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
            INodeDouble angle = GenericExpressionCompiler.compileExpressionDouble(arguments[0], functions).derive(null);
            INodeDouble x = GenericExpressionCompiler.compileExpressionDouble(arguments[1], functions).derive(null);
            INodeDouble y = GenericExpressionCompiler.compileExpressionDouble(arguments[2], functions).derive(null);
            INodeDouble z = GenericExpressionCompiler.compileExpressionDouble(arguments[3], functions).derive(null);
            return new BakedRotationFunctional(angle, x, y, z);
        } else if (function.equalsIgnoreCase("scale")) {
            INodeDouble x = GenericExpressionCompiler.compileExpressionDouble(arguments[0], functions).derive(null);
            INodeDouble y = GenericExpressionCompiler.compileExpressionDouble(arguments[1], functions).derive(null);
            INodeDouble z;
            if (arguments.length == 3) z = new NodeImmutableDouble(1);
            else z = GenericExpressionCompiler.compileExpressionDouble(arguments[2], functions).derive(null);
            return new BakedScaleFunctional(x, y, z);
        } else if (function.equalsIgnoreCase("colour")) {
            INodeDouble r = GenericExpressionCompiler.compileExpressionDouble(arguments[0], functions).derive(null);
            INodeDouble g = GenericExpressionCompiler.compileExpressionDouble(arguments[1], functions).derive(null);
            INodeDouble b = GenericExpressionCompiler.compileExpressionDouble(arguments[2], functions).derive(null);
            INodeDouble alpha;
            if (arguments.length == 3) alpha = new NodeImmutableDouble(1);
            else alpha = GenericExpressionCompiler.compileExpressionDouble(arguments[3], functions).derive(null);
            return new BakedColourFunctional(r, g, b, alpha);
        } else if (function.equalsIgnoreCase("position")) {
            INodeDouble x = GenericExpressionCompiler.compileExpressionDouble(arguments[0], functions).derive(null);
            INodeDouble y = GenericExpressionCompiler.compileExpressionDouble(arguments[1], functions).derive(null);
            INodeDouble z;
            if (arguments.length == 2) z = new NodeImmutableDouble(0);
            else z = GenericExpressionCompiler.compileExpressionDouble(arguments[2], functions).derive(null);
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
