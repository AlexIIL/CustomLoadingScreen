package alexiil.mc.mod.load.json.subtypes;

import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.baked.factory.BakedFactoryStatus;
import alexiil.mc.mod.load.json.ConfigManager;
import alexiil.mc.mod.load.json.JsonFactory;
import alexiil.mc.mod.load.json.JsonRenderingPart;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.GenericExpressionCompiler;
import buildcraft.lib.expression.InvalidExpressionException;
import buildcraft.lib.expression.api.IExpressionNode.INodeBoolean;

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
        INodeBoolean destroy = GenericExpressionCompiler.compileExpressionBoolean(shouldDestroy, functions);

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
