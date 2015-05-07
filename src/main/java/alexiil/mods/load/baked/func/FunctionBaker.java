package alexiil.mods.load.baked.func;

import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import alexiil.mods.load.baked.func.BakedPostFixFunction.IBakedStackFunction;
import alexiil.mods.load.baked.func.stack.BakedStackCastInteger;
import alexiil.mods.load.baked.func.stack.BakedStackFunctionCaller;
import alexiil.mods.load.baked.func.stack.BakedStackValue;
import alexiil.mods.load.baked.func.stack.op.*;
import alexiil.mods.load.baked.func.stack.var.BakedStackVariable;

public class FunctionBaker {
    private static final String VALID_CHARACHTERS = "abcdefghijklmnopqrstuvwxyz_'";
    private static final String HEX_DIGITS = "0123456789abcdef";

    // ^ is mathematical power
    // <Double> ^ <Double> returns <Double>

    // & is boolean AND operation
    // <Boolean> & <Boolean> returns <Boolean>

    // | is boolean OR operation
    // <Boolean> & <Boolean> returns <Boolean>

    // <, >, >=, <= are mathematical comparing operations
    // <Double> = <Double> returns <Boolean>

    // = is equality tester
    // <Object> = <Object> returns <Boolean>

    // != is inequality tester
    // <Object> != <Object> returns <Boolean>

    // ? and : are used for either one or the other
    // ( <Boolean> ? <Object> : <Object> ) returns <Object>
    private static final String OPERATORS = "()^*/+-&|<=>=!=?:";
    private static final String SINGLE_LETTER_OPERATORS = "()^*/+-&|?:";
    private static final String[] OPERATOR_PRECEDENCE = new String[] { "&|", "?:", "<=>=", "+-", "*/", "^" };

    // 0 = number literal
    // 1 = alphabetic char
    // 2 = operator
    private static int getType(String chr) {
        if (chr.startsWith("0x"))
            return 0;
        if (VALID_CHARACHTERS.contains(chr))
            return 1;
        if (OPERATORS.contains(chr))
            return 2;
        return 0;
    }

    private static int getPrecedence(String token) {
        for (int i = 0; i < OPERATOR_PRECEDENCE.length; i++) {
            String ops = OPERATOR_PRECEDENCE[i];
            if (ops.contains(token))
                return i;
        }
        return -1;
    }

    private static IBakedStackFunction getForToken(String token) {
        if (token.equals("+")) {
            return new BakedStackOperationAddition();
        }
        else if (token.equals("-")) {
            return (new BakedStackOperationSubtraction());
        }
        else if (token.equals("*")) {
            return new BakedStackOperationMultiply();
        }
        else if (token.equals("/")) {
            return new BakedStackOperationDivision();
        }
        else if (token.equals("^")) {
            return new BakedStackOperationPower();
        }
        else if (token.equals("=")) {
            return new BakedStackOperationEquality();
        }
        else if (token.equals("?")) {
            return new BakedStackOperationConditional();
        }
        else if (token.equals("<")) {
            return new BakedStackOperationLess();
        }
        else if (token.equals(">")) {
            return new BakedStackOperationGreater();
        }
        else if (token.equals("<=")) {
            return new BakedStackOperationLessOrEqual();
        }
        else if (token.equals(">=")) {
            return new BakedStackOperationGreaterOrEqual();
        }
        else if (token.equals("&")) {
            return new BakedStackOperationAnd();
        }
        else if (token.equals("|")) {
            return new BakedStackOperationOr();
        }
        throw new Error(token + " was not a valid token!");
    }

    private static List<IBakedStackFunction> infixToPostfix(String infix, Map<String, IBakedFunction<?>> functions) {
        infix = infix.toLowerCase(Locale.ROOT);

        List<IBakedStackFunction> list = Lists.newArrayList();
        Deque<String> stack = Queues.newArrayDeque();

        while (infix.length() > 0) {
            int lastType = getType(infix.substring(0, 1));
            String token = "";
            int pos = 0;
            boolean quotes = infix.startsWith("'");
            if (quotes) {
                pos = 1;
                token = "'";
            }
            while (pos < infix.length()) {
                String atPos = infix.substring(pos, pos + 1);
                if (quotes) {
                    if (!atPos.startsWith("'")) {
                        token += atPos;
                        pos++;
                        continue;
                    }
                    else {
                        token += "'";
                        break;
                    }
                }
                int type;
                if (token.equals("0") && atPos.equals("x"))
                    type = 0;
                else if (token.startsWith("0x") && HEX_DIGITS.contains(atPos))
                    type = 0;
                else
                    type = getType(atPos);
                if (type != lastType) {
                    // lastType = type;
                    break;
                }
                else
                    token += atPos;
                if (SINGLE_LETTER_OPERATORS.contains(token))
                    break;
                pos++;
            }
            lastType = getType(token.substring(0, 1));
            infix = infix.substring(token.length());
            if (lastType == 0) {// Number => should push itself onto the stack
                if (token.startsWith("0x")) {// Hex => should transform to normal int, then push itself
                    int value = Integer.parseInt(token.substring(2), 16);
                    list.add(new BakedStackValue<Double>((double) value));
                }
                else
                    list.add(new BakedStackValue<Double>(Double.valueOf(token)));
            }
            else if (lastType == 1) {
                if (token.startsWith("'") && token.endsWith("'") && token.length() > 1) {
                    list.add(new BakedStackValue<String>(token.substring(1, token.length() - 1)));
                }
                else if (token.equals("integer")) {
                    list.add(new BakedStackCastInteger());
                }
                else if (token.equals("variable")) {
                    list.add(new BakedStackVariable());
                }
                else if (token.equals("super")) {
                    throw new Error("Found a 'super' token, this function is not available in the current context!");
                }
                else {
                    boolean found = false;
                    for (Entry<String, IBakedFunction<?>> entry : functions.entrySet()) {
                        if (token.equalsIgnoreCase(entry.getKey())) {
                            list.add(new BakedStackFunctionCaller(entry.getValue()));
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        throw new Error(token + " was not found");
                }
            }
            else if (lastType == 2) {
                if (token.equals("("))
                    stack.push(token);
                else if (token.equals(":"))
                    ;// Ignore colons, as these are used for spacing stuffs. Ok fine, this isn't great in terms of
                     // functions, but until sin(argument) like stuff is proeprly implemented, this is all we get
                else if (token.equals(")")) {
                    while (true) {
                        if (stack.isEmpty())
                            throw new Error("Mismatched parentheses for function \"" + infix + "\"");
                        String onStack = stack.pop();
                        if (!onStack.equals("(")) {
                            list.add(getForToken(onStack));
                        }
                        else if (onStack.equals("("))
                            break;
                    }
                }
                else if (stack.isEmpty()) {
                    stack.add(token);
                }
                else {
                    while (!stack.isEmpty()) {
                        int stackPrec = getPrecedence(stack.peek());
                        int tokenPrec = getPrecedence(token);

                        boolean minus = token.equals("-");
                        if (minus)
                            ;// FIXME: FunctionBaker cannot take "-32" as a negative number (etc)

                        if (tokenPrec >= stackPrec)
                            break;
                        else {
                            String popped = stack.pop();
                            list.add(getForToken(popped));
                        }
                    }
                    stack.push(token);
                }
            }
        }
        while (!stack.isEmpty()) {
            list.add(getForToken(stack.pop()));
        }
        return list;
    }

    public static <T> IBakedFunction<T> bakeFunction(String function, Map<String, IBakedFunction<?>> functions) {
        List<IBakedStackFunction> postfix = infixToPostfix(function.replace(" ", ""), functions);
        return new BakedPostFixFunction<T>(postfix, function);
    }

    public static <T> IBakedFunction<T> bakeFunction(String function) {
        return bakeFunction(function, Collections.<String, IBakedFunction<?>> emptyMap());
    }

    public static IBakedFunction<Double> bakeFunctionDouble(String function, Map<String, IBakedFunction<?>> functions) {
        return bakeFunction(function, functions);
    }

    public static IBakedFunction<Double> bakeFunctionDouble(String function) {
        return bakeFunction(function);
    }

    public static IBakedFunction<String> bakeFunctionString(String function, Map<String, IBakedFunction<?>> functions) {
        return bakeFunction(function, functions);
    }

    public static IBakedFunction<String> bakeFunctionString(String function) {
        return bakeFunction(function);
    }

    public static IBakedFunction<Boolean> bakeFunctionBoolean(String function, Map<String, IBakedFunction<?>> functions) {
        return bakeFunction(function, functions);
    }

    public static IBakedFunction<Boolean> bakeFunctionBoolean(String function) {
        return bakeFunction(function);
    }

    /** Used to expand a function to include what its parent has for this function. This only works if the parent is the
     * same function as the child: so you can use this for properties of a function. The parent MUST have been replaced
     * by a similar function to this if this is to be passed directly to a bakeFunction() method. */
    public static String expandParents(String function, String parent) {
        return function.replace("super", "(" + parent + ")");
    }
}
