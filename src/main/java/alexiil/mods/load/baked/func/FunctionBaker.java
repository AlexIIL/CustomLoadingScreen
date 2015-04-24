package alexiil.mods.load.baked.func;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import alexiil.mods.load.baked.func.BakedPostFixFunction.IBakedStackFunction;
import alexiil.mods.load.baked.func.stack.BakedStackCastInteger;
import alexiil.mods.load.baked.func.stack.BakedStackFunctionCaller;
import alexiil.mods.load.baked.func.stack.BakedStackValue;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationAddition;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationAnd;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationConditional;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationDivision;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationEquality;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationGreater;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationGreaterOrEqual;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationLess;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationLessOrEqual;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationMultiply;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationOr;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationPower;
import alexiil.mods.load.baked.func.stack.op.BakedStackOperationSubtraction;
import alexiil.mods.load.baked.func.stack.var.BakedStackVariable;
import alexiil.mods.load.baked.func.stack.var.BakedStackVariablePercentage;
import alexiil.mods.load.baked.func.stack.var.BakedStackVariableScreenHeight;
import alexiil.mods.load.baked.func.stack.var.BakedStackVariableScreenWidth;
import alexiil.mods.load.baked.func.stack.var.BakedStackVariableSeconds;
import alexiil.mods.load.baked.func.stack.var.BakedStackVariableStatus;

public class FunctionBaker {
    private static final String VALID_CHARACHTERS = "abcdefghijklmnopqrstuvwxyz_'";

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

        List<IBakedStackFunction> list = new ArrayList<IBakedStackFunction>();
        Deque<String> stack = new ArrayDeque<String>();

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
                int type = getType(atPos);
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
                list.add(new BakedStackValue<Double>(Double.valueOf(token)));
            }
            else if (lastType == 1) {
                if (token.equals("true")) {
                    list.add(new BakedStackValue<Boolean>(true));
                }
                else if (token.equals("false")) {
                    list.add(new BakedStackValue<Boolean>(false));
                }
                else if (token.equals("status")) {
                    list.add(new BakedStackVariableStatus());
                }
                else if (token.equals("percentage")) {
                    list.add(new BakedStackVariablePercentage());
                }
                else if (token.equals("screenwidth")) {
                    list.add(new BakedStackVariableScreenWidth());
                }
                else if (token.equals("screenheight")) {
                    list.add(new BakedStackVariableScreenHeight());
                }
                else if (token.equals("seconds")) {
                    list.add(new BakedStackVariableSeconds());
                }
                else if (token.startsWith("'") && token.endsWith("'") && token.length() > 1) {
                    list.add(new BakedStackValue<String>(token.substring(1, token.length() - 1)));
                }
                else if (token.equals("integer")) {
                    list.add(new BakedStackCastInteger());
                }
                else if (token.equals("variable")) {
                    list.add(new BakedStackVariable());
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
                    ;
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
                            ;// TODO: FIX NEGATIVE NUMBERS

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

    public static <T> IBakedFunction<T> bakeFunction(String function) {
        return bakeFunction(function, Collections.<String, IBakedFunction<?>> emptyMap());
    }

    public static <T> IBakedFunction<T> bakeFunction(String function, Map<String, IBakedFunction<?>> functions) {
        List<IBakedStackFunction> postfix = infixToPostfix(function.replace(" ", ""), functions);
        return new BakedPostFixFunction<T>(postfix, function);
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
}
