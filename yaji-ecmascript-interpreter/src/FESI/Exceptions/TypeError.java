package FESI.Exceptions;

public class TypeError extends EcmaScriptException {

    public TypeError(String reason, Throwable originatingException) {
        super(reason, originatingException);
    }

    public TypeError(String reason) {
        super(reason);
    }

    private static final long serialVersionUID = -4837134347404093555L;

}
