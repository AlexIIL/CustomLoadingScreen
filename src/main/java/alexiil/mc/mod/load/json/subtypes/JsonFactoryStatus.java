package alexiil.mc.mod.load.json.subtypes;

import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.factory.BakedFactoryStatus;
import alexiil.mc.mod.load.expression.FunctionContext;
import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.InvalidExpressionException;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonFactory;
import alexiil.mc.mod.load.json.JsonRenderingPart;

public class JsonFactoryStatus extends JsonFactory {
    public JsonFactoryStatus(String shouldDestroy, String toCreate) {
        // None of the arguments matter
        super("false", shouldDestroy, toCreate);
    }

    public JsonFactoryStatus() {
        // None of the arguments matter
        super("false", "false", null);
    }

    @Override
    public BakedFactoryStatus actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        INodeBoolean destroy = GenericExpressionCompiler.compileExpressionBoolean(shouldDestroy, functions).derive(null);

        JsonRenderingPart jrp = ConfigManager.getAsRenderingPart(toCreate);
        if (jrp == null) {

            return null;
        }
        jrp = jrp.getConsolidated();
        BakedRenderingPart component = jrp.bake(functions);
        return new BakedFactoryStatus(destroy, component);
    }

    /** This is overridden just so that the correct type is returned, with no casting needed. Note that the children
     * will have to consolidate down to this class, otherwise it won't work properly. */
    @Override
    protected JsonFactoryStatus actuallyConsolidate() {
        return this;
    }
}
