package alexiil.mc.mod.load.baked.func;

@SuppressWarnings("serial")
public class FunctionException extends Exception {
    public FunctionException(String function, String error) {
        super("The function \"" + function + "\" failed because " + error);
    }

    public FunctionException(String function, String error, Throwable t) {
        super("The function \"" + function + "\" failed because " + error, t);
    }
}
