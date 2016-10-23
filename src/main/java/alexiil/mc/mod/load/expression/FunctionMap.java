package alexiil.mc.mod.load.expression;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;

import alexiil.mc.mod.load.expression.api.FunctionIdentifier;
import alexiil.mc.mod.load.expression.api.IExpression;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionBoolean;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionDouble;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionLong;
import alexiil.mc.mod.load.expression.api.IExpression.IExpressionString;
import alexiil.mc.mod.load.expression.api.IFunctionMap;

public class FunctionMap implements IFunctionMap {
    private final InnerMap<IExpressionLong> longFunctions = new InnerMap<>();
    private final InnerMap<IExpressionDouble> doubleFunctions = new InnerMap<>();
    private final InnerMap<IExpressionBoolean> booleanFunctions = new InnerMap<>();
    private final InnerMap<IExpressionString> stringFunctions = new InnerMap<>();

    public FunctionMap() {}

    public FunctionMap(IFunctionMap from) {
        longFunctions.copyFrom(from.getLongMap());
        doubleFunctions.copyFrom(from.getDoubleMap());
        booleanFunctions.copyFrom(from.getBooleanMap());
        stringFunctions.copyFrom(from.getStringMap());
    }

    @Override
    public IInnerMap<IExpressionLong> getLongMap() {
        return longFunctions;
    }

    @Override
    public IInnerMap<IExpressionDouble> getDoubleMap() {
        return doubleFunctions;
    }

    @Override
    public IInnerMap<IExpressionBoolean> getBooleanMap() {
        return booleanFunctions;
    }

    @Override
    public IInnerMap<IExpressionString> getStringMap() {
        return stringFunctions;
    }

    public class InnerMap<E extends IExpression> implements IInnerMap<E> {
        private final Map<FunctionIdentifier, E> functions = new HashMap<>();
        private final Multimap<String, E> functionNames = HashMultimap.create();

        @Override
        public FunctionIdentifier putExpression(String name, E expression) {
            FunctionIdentifier ident = new FunctionIdentifier(name, expression.getCounts());

            IExpression existing = FunctionMap.this.getExpression(ident);
            if (existing != null) {
                throw new IllegalStateException("You cannot add multiple functions with differing return types!");
            }

            E old = functions.put(ident, expression);
            if (old != null) {
                functionNames.remove(ident.lowerCaseName, old);
            }
            functionNames.put(ident.lowerCaseName, expression);

            GenericExpressionCompiler.debugPrintln("Defined a function " + ident);

            return ident;
        }

        public void copyFrom(IInnerMap<E> from) {
            functions.clear();
            functionNames.clear();
            for (Entry<FunctionIdentifier, E> entry : from.getAllExpressions().entrySet()) {
                FunctionIdentifier fi = entry.getKey();
                E exp = entry.getValue();
                functions.put(fi, exp);
                functionNames.put(fi.lowerCaseName, exp);
            }
        }

        @Override
        public Collection<E> getExpressions(String name) {
            return ImmutableList.copyOf(functionNames.get(name));
        }

        @Override
        public Collection<E> getExpressions(String name, int numArgs) {
            ImmutableList.Builder<E> matching = ImmutableList.builder();
            for (E element : functionNames.get(name)) {
                if (element.getCounts().order.size() == numArgs) {
                    matching.add(element);
                }
            }
            return matching.build();
        }

        @Override
        public E getExpression(FunctionIdentifier identifer) {
            return functions.get(identifer);
        }

        @Override
        public Map<FunctionIdentifier, E> getAllExpressions() {
            return functions;
        }
    }
}
