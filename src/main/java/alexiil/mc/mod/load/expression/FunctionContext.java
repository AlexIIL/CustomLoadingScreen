package alexiil.mc.mod.load.expression;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import alexiil.mc.mod.load.expression.api.IExpressionNode;
import alexiil.mc.mod.load.expression.api.IFunctionContext;
import alexiil.mc.mod.load.expression.api.IFunctionMap;
import alexiil.mc.mod.load.expression.node.value.NodeMutableBoolean;
import alexiil.mc.mod.load.expression.node.value.NodeMutableDouble;
import alexiil.mc.mod.load.expression.node.value.NodeMutableLong;
import alexiil.mc.mod.load.expression.node.value.NodeMutableString;

/** Holds a set of function-local variables that can be called upon by the expression. */
public class FunctionContext implements IFunctionContext {
    private final IFunctionMap map;
    private final Map<String, IExpressionNode> allNodes = new HashMap<>();
    private final Map<String, NodeMutableBoolean> booleans = new HashMap<>();
    private final Map<String, NodeMutableDouble> doubles = new HashMap<>();
    private final Map<String, NodeMutableLong> longs = new HashMap<>();
    private final Map<String, NodeMutableString> strings = new HashMap<>();

    public FunctionContext(IFunctionMap map) {
        this.map = map;
    }

    public FunctionContext() {
        this.map = new FunctionMap();
    }

    public FunctionContext(FunctionContext from) {
        this.map = new FunctionMap(from.map);
        allNodes.putAll(from.allNodes);
        booleans.putAll(from.booleans);
        doubles.putAll(from.doubles);
        longs.putAll(from.longs);
        strings.putAll(from.strings);
    }

    public IFunctionMap getFunctionMap() {
        return map;
    }

    public NodeMutableBoolean getOrAddBoolean(String name) {
        return getOrAdd(name, booleans, new NodeMutableBoolean());
    }

    public NodeMutableBoolean getBoolean(String name) {
        return booleans.get(name);
    }

    public NodeMutableDouble getOrAddDouble(String name) {
        return getOrAdd(name, doubles, new NodeMutableDouble());
    }

    public NodeMutableBoolean getDouble(String name) {
        return booleans.get(name);
    }

    public NodeMutableLong getOrAddLong(String name) {
        return getOrAdd(name, longs, new NodeMutableLong());
    }

    public NodeMutableBoolean getLong(String name) {
        return booleans.get(name);
    }

    public NodeMutableString getOrAddString(String name) {
        return getOrAdd(name, strings, new NodeMutableString());
    }

    public NodeMutableBoolean getString(String name) {
        return booleans.get(name);
    }

    private <N extends IExpressionNode> N getOrAdd(String name, Map<String, N> toAddTo, N instance) {
        name = name.toLowerCase(Locale.ROOT);
        N existant = get(name);
        if (existant != null) {
            return existant;
        }
        toAddTo.put(name, instance);
        allNodes.put(name, instance);
        return instance;
    }

    public IExpressionNode getAny(String name) {
        return allNodes.get(name.toLowerCase(Locale.ROOT));
    }

    private <N extends IExpressionNode> N get(String name) {
        return (N) getAny(name);
    }
}
