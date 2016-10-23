package alexiil.mc.mod.load.json;

import org.apache.commons.lang3.StringUtils;

import alexiil.mc.mod.load.baked.BakedFactory;
import alexiil.mc.mod.load.baked.BakedRenderingPart;
import alexiil.mc.mod.load.expression.FunctionContext;
import alexiil.mc.mod.load.expression.GenericExpressionCompiler;
import alexiil.mc.mod.load.expression.InvalidExpressionException;
import alexiil.mc.mod.load.expression.api.IExpressionNode.INodeBoolean;
import alexiil.mc.mod.load.json.subtypes.JsonFactoryStatus;

public class JsonFactory extends JsonConfigurable<JsonFactory, BakedFactory> {
    public final String shouldCreate, shouldDestroy, toCreate;

    public JsonFactory(String shouldCreate, String shouldDestroy, String toCreate) {
        super("");
        this.shouldCreate = shouldCreate;
        this.shouldDestroy = shouldDestroy;
        this.toCreate = toCreate;
    }

    @Override
    public BakedFactory actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        INodeBoolean create = GenericExpressionCompiler.compileExpressionBoolean(this.shouldCreate, functions).derive(null);
        INodeBoolean destroy = GenericExpressionCompiler.compileExpressionBoolean(this.shouldDestroy, functions).derive(null);
        JsonRenderingPart jrp = ConfigManager.getAsRenderingPart(toCreate).getConsolidated();
        BakedRenderingPart component = jrp.bake(functions);
        return new BakedFactory(create, destroy, component);
    }

    @Override
    protected JsonFactory actuallyConsolidate() {
        if (StringUtils.isEmpty(parent)) return this;
        JsonFactory jParent = ConfigManager.getAsFactory(this.parent).getConsolidated();
        if (jParent instanceof JsonFactoryStatus) {
            return new JsonFactoryStatus(shouldDestroy, toCreate);
        }
        String create = consolidateFunction(shouldCreate, jParent.shouldCreate, "false");
        String destroy = consolidateFunction(shouldDestroy, jParent.shouldDestroy, "false");
        String part = overrideObject(toCreate, jParent.toCreate, "");
        return new JsonFactory(create, destroy, part);
    }
}