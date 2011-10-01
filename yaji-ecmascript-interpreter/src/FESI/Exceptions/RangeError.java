package FESI.Exceptions;

import FESI.Data.NativeErrorObject;

public class RangeError extends EcmaScriptException {

    public RangeError(String reason, Throwable originatingException) {
        super(reason, originatingException, NativeErrorObject.RANGE_ERROR);
    }

    public RangeError(String reason) {
        super(reason, NativeErrorObject.RANGE_ERROR);
    }

    private static final long serialVersionUID = -4837134347404093555L;

}
