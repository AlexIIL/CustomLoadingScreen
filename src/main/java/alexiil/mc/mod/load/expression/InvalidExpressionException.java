package alexiil.mc.mod.load.expression;

@SuppressWarnings("serial")
public class InvalidExpressionException extends Exception {
    public InvalidExpressionException(String message) {
        super(message);
    }

    public InvalidExpressionException(String message, Throwable cause) {
        super(message, cause);
    }
}
