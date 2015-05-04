package alexiil.mods.load;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sun.xml.internal.ws.org.objectweb.asm.Opcodes;

/** This is just a few methods to reduce the size of logging calls. None of them are complex and just delegate methods
 * for Logger.log. */
public class BLSLog {
    private static Logger log;

    public static void info(String toLog) {
        log(Level.INFO, toLog);
    }

    public static void warn(String text) {
        log(Level.WARN, text);
    }

    public static void warn(String message, Throwable thrown) {
        log(Level.WARN, message, thrown);
    }

    public static void trace(String message) {
        log(Level.TRACE, message);
    }

    public static void log(Level level, String text) {
        log().log(level, text);
    }

    public static void log(Level level, String message, Throwable t) {
        log().log(level, message, t);
    }

    private static Logger log() {
        if (log == null)
            log = LogManager.getLogger("BetterLoadingScreen");
        return log;
    }

    /** Use this for temporary logging, so eclipse helps see where every temporary logging statement is (so cleanup is
     * much easier) */
    @Deprecated
    public static void temp(String text) {
        info(text);
    }

    // /////////////////////////////////////////////////////////////////////////////////////////
    //
    // Used by the LogHelperTransformer to log trace information
    //
    // /////////////////////////////////////////////////////////////////////////////////////////

    private static final ThreadLocal<Integer> stack = new ThreadLocal<Integer>();

    private static int getStack() {
        Integer i = stack.get();
        if (i == null)
            return 0;
        return i;
    }

    private static void logThing(String place, String arg, int indent) {
        StackTraceElement ste = Thread.currentThread().getStackTrace()[indent];
        String clazz = ste.getClassName().substring(ste.getClassName().lastIndexOf('.') + 1);
        String before = StringUtils.repeat("  ", getStack()) + place + " " + clazz + "." + ste.getMethodName();
        info(before + arg);
    }

    private static void logThing(String place, Object... args) {
        String spaces = StringUtils.repeat("  ", getStack() + 28);
        String arguments = "(";
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            String s = arg == null ? "null" : arg.toString();
            s = s.replace("\n", "\n" + spaces + "  ");
            if (s.length() > 30) {
                s = "\n" + spaces + "  " + s + "\n" + spaces;
            }

            if (arg instanceof String)
                s = "\"" + s + "\"";

            arguments += s;
            if (i < args.length - 1)
                arguments += ", ";
        }
        arguments += ")";
        logThing(place, arguments, 4);
    }

    private static String makeNewLine(String string) {
        if (string.length() < 30)
            return string;
        String spaces = StringUtils.repeat("  ", getStack() + 28);
        string = string.replace("\n", "\n" + spaces + "  ");
        if (string.length() > 30) {
            string = "\n" + spaces + "  " + string + "\n" + spaces;
        }
        return string;
    }

    public static void justInvoke() {
        stack.set(getStack() + 1);
    }

    public static void justReturn() {
        stack.set(getStack() - 1);
    }

    public static void logInvoke(Object... args) {
        logThing("invoke", args);
        justInvoke();
    }

    public static void logReturn() {
        justReturn();
        logThing("return");
    }

    public static void logReturn(Object arg) {
        justReturn();
        logThing("return", arg);
    }

    public static void logIf(Object arg1, Object arg2, int opcode) {
        String text;
        boolean returnVal = false;
        switch (opcode) {
            case Opcodes.IFNULL:// Object == null
            case Opcodes.IF_ACMPEQ: {// Object Equal
                text = arg1 + " == " + arg2;
                returnVal = arg1 == arg2;
                break;
            }
            case Opcodes.IFNONNULL:// Object != null
            case Opcodes.IF_ACMPNE: {// Object Not Equal
                text = arg1 + " != " + arg2;
                returnVal = arg1 != arg2;
                break;
            }
            case Opcodes.IF_ICMPEQ:// Equal
            case Opcodes.IFEQ: {
                text = arg1 + " == " + arg2;
                returnVal = arg1 != arg2;
                break;
            }
            case Opcodes.IF_ICMPNE:// Not Equal
            case Opcodes.IFNE: {
                text = arg1 + " != " + arg2;
                returnVal = arg1 != arg2;
                break;
            }
            case Opcodes.IF_ICMPLE:// Less Than or Equal
            case Opcodes.IFLE: {
                text = arg1 + " <= " + arg2;
                returnVal = ((Double) arg1) <= ((Double) arg2);
                break;
            }
            case Opcodes.IF_ICMPLT:// Less Than
            case Opcodes.IFLT: {
                text = arg1 + " < " + arg2;
                returnVal = ((Double) arg1) < ((Double) arg2);
                break;
            }
            case Opcodes.IF_ICMPGE:// Greater Than or Equal
            case Opcodes.IFGE: {
                text = arg1 + " >= " + arg2;
                returnVal = ((Double) arg1) >= ((Double) arg2);
                break;
            }
            case Opcodes.IF_ICMPGT:// Greater Than
            case Opcodes.IFGT: {
                text = arg1 + " > " + arg2;
                returnVal = ((Double) arg1) > ((Double) arg2);
                break;
            }
            default: {
                text = "Unknown Opcode " + opcode;
            }
        }
        logThing("if", "(" + makeNewLine(text) + ") returned " + returnVal, 3);
    }
}
