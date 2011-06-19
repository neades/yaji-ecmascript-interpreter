package FESI.Exceptions;

import FESI.Data.NativeErrorObject;

public class ReferenceError extends EcmaScriptException {

    private static final long serialVersionUID = -1791812974092352430L;

    public ReferenceError(String reason, Throwable originatingException) {
        super(reason, originatingException, NativeErrorObject.REFERENCE_ERROR);
    }

    public ReferenceError(String reason) {
        super(reason, NativeErrorObject.REFERENCE_ERROR);
    }


}
