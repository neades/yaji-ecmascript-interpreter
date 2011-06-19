package FESI.Exceptions;

import FESI.Data.NativeErrorObject;

public class TypeError extends EcmaScriptException {

    public TypeError(String reason, Throwable originatingException) {
        super(reason, originatingException, NativeErrorObject.TYPE_ERROR);
    }

    public TypeError(String reason) {
        super(reason, NativeErrorObject.TYPE_ERROR);
    }

    private static final long serialVersionUID = -4837134347404093555L;

}
