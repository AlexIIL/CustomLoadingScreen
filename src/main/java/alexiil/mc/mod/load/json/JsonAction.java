package alexiil.mc.mod.load.json;

import alexiil.mc.mod.load.baked.BakedAction;

public abstract class JsonAction extends JsonConfigurable<JsonAction, BakedAction> {
    public final String conditionStart;
    public final String conditionEnd;

    public JsonAction(String conditionStart, String conditionEnd) {
        this.conditionStart = conditionStart;
        this.conditionEnd = conditionEnd;
    }
}
