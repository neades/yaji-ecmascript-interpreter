package FESI.Interpreter;

import FESI.AST.SimpleNode;
import FESI.Exceptions.EcmaScriptException;

/**
 * Exception used to package any exception encountered during visiting to an
 * exception accepted by the visitor interface as defined by JavaCC. Eventually
 * the exception will be unpackaged and reraised.
 */
public class PackagedException extends RuntimeException {
    private static final long serialVersionUID = -5115990393628214347L;
    EcmaScriptException exception;
    SimpleNode node;

    public PackagedException(EcmaScriptException exception, SimpleNode node) {
        super();
        this.exception = exception;
        this.node = node;
    }

    @Override
    public String getMessage() {
        return exception.getMessage();
    }

    public Exception getPackage() {
        return exception;
    }
}