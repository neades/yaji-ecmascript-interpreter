package FESI.Exceptions;

import FESI.Data.NativeErrorObject;

public class URIError extends EcmaScriptException {

    private static final long serialVersionUID = -2236372255400051725L;

    public URIError(String reason, Throwable originatingException) {
        super(reason, originatingException, NativeErrorObject.URI_ERROR);
    }

    public URIError(String reason) {
        super(reason, NativeErrorObject.URI_ERROR);
    }


}
