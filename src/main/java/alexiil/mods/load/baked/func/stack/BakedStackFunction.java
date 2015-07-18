package alexiil.mods.load.baked.func.stack;

import java.util.Deque;

import alexiil.mods.load.baked.func.BakedPostFixFunction.IBakedStackFunction;

public abstract class BakedStackFunction implements IBakedStackFunction {
    @SuppressWarnings("rawtypes")
    protected Double popDouble(Deque stack) throws StackFunctionException {
        if (stack.isEmpty()) {
            throw new StackFunctionException("The stack was empty when attempting to pop a double from it!");
        }
        Object obj = stack.peek();
        try {
            Double var = (Double) obj;
            stack.pop();// Bit long, but this makes it throw a class cast exception BEFORE
            return var;
        } catch (ClassCastException cce) {
            throw new StackFunctionException(obj + "(" + obj.getClass() + ") is not a double! (" + Double.class + ")");
        }
    }

    @SuppressWarnings("rawtypes")
    protected Boolean popBoolean(Deque stack) throws StackFunctionException {
        if (stack.isEmpty()) {
            throw new StackFunctionException("The stack was empty when attempting to pop a double from it!");
        }
        Object obj = stack.peek();
        try {
            Boolean var = (Boolean) obj;
            stack.pop();// Bit long, but this makes it throw a class cast exception BEFORE
            return var;
        } catch (ClassCastException cce) {
            throw new StackFunctionException(obj + "(" + obj.getClass() + ") is not a boolean! (" + Boolean.class + ")");
        }
    }

    @SuppressWarnings("rawtypes")
    protected String popString(Deque stack) throws StackFunctionException {
        if (stack.isEmpty()) {
            throw new StackFunctionException("The stack was empty when attempting to pop a double from it!");
        }
        Object obj = stack.peek();
        try {
            String var = (String) obj;
            stack.pop();// Bit long, but this makes it throw a class cast exception BEFORE
            return var;
        } catch (ClassCastException cce) {
            throw new StackFunctionException(obj + "(" + obj.getClass() + ") is not a string! (" + String.class + ")");
        }
    }

    protected Object popObject(@SuppressWarnings("rawtypes") Deque stack) throws StackFunctionException {
        if (stack.isEmpty()) {
            throw new StackFunctionException("The stack was empty when attempting to pop a value from it!");
        }
        return stack.pop();
    }
}
