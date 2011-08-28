package FESI.Exceptions;

import FESI.Data.NativeErrorObject;

public class SyntaxError extends EcmaScriptException {

    private static final long serialVersionUID = 1945152265651929410L;

    public SyntaxError(String reason, Throwable originatingException) {
        super(reason, originatingException, NativeErrorObject.SYNTAX_ERROR);
    }

    public SyntaxError(String reason) {
        super(reason, NativeErrorObject.SYNTAX_ERROR);
    }


}
