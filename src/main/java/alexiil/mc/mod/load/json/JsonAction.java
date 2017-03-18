package alexiil.mc.mod.load.json;

import org.apache.commons.lang3.StringUtils;

import alexiil.mc.mod.load.baked.BakedAction;
import alexiil.mc.mod.load.json.subtypes.JsonActionSound;

import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.InvalidExpressionException;

public class JsonAction extends JsonConfigurable<JsonAction, BakedAction> {
    public final String conditionStart;
    public final String conditionEnd;
    public final String[] arguments;

    public JsonAction(String parent, String conditionStart, String conditionEnd, String[] arguments) {
        super(parent);
        this.conditionStart = conditionStart;
        this.conditionEnd = conditionEnd;
        this.arguments = arguments;
    }

    @Override
    protected BakedAction actuallyBake(FunctionContext functions) throws InvalidExpressionException {
        throw new IllegalArgumentException("You cannot bake an action wth no parent!");
    }

    @Override
    protected JsonAction actuallyConsolidate() {
        if (StringUtils.isEmpty(parent)) return this;

        JsonAction jParent = ConfigManager.getAsAction(parent).getConsolidated();
        String conditionStart = consolidateFunction(this.conditionStart, jParent.conditionStart, "false");
        String conditionEnd = consolidateFunction(this.conditionEnd, jParent.conditionEnd, "true");
        String[] arguments = overrideArray(this.arguments, jParent.arguments);

        if (jParent instanceof JsonActionSound) {
            return new JsonActionSound(resourceLocation, conditionStart, conditionEnd, arguments);
        }

        return new JsonAction("", conditionStart, conditionEnd, arguments);

    }
}
