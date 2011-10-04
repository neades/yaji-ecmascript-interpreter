package FESI.Bsf;

import FESI.jslib.*;

import java.util.Vector;

import org.apache.bsf.*;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.BSFFunctions;

/**
 * This is the interface to FESI (ecmascript) from the Bean Scripting Framework.
 * <p>
 * Based on BSF example of interface to Javascript
 */
public class BsfEngine extends BSFEngineImpl {
    /**
     * The global script object, where all embedded functions are defined, as
     * well as the standard ECMA "core" objects.
     */
    private JSGlobalObject global;

    /**
     * initialize the engine. put the manager into the context -> manager map
     * hashtable too.
     */
    @SuppressWarnings({ "rawtypes" })
    public void initialize(BSFManager mgr, String lang, Vector declaredBeans)
            throws BSFException {
        super.initialize(mgr, lang, declaredBeans);

        // Initialize global scope object
        try {

            global = JSUtil.makeEvaluator();
            BSFFunctions bsf = new BSFFunctions(mgr, this);
            global.setMember("bsf", bsf);

            int size = declaredBeans.size();
            for (int i = 0; i < size; i++) {
                declareBean((BSFDeclaredBean) declaredBeans.elementAt(i));
            }
        } catch (Throwable t) { // includes JavaScriptException, rethrows Errors
            handleError(t);
        }
    }

    /**
     * This is used by an application to evaluate a string containing some
     * expression.
     */
    public Object eval(String source, int lineNo, int columnNo, Object oscript)
            throws BSFException {
        String script = oscript.toString();
        Object retval = null;
        try {
            // Fesi unwrap the returned object if needed
            retval = global.eval(script);
        } catch (Throwable t) { // includes JavaScriptException, rethrows Errors
            handleError(t);
        }
        return retval;
    }

    /*
     * *
     * This is used by an application to evaluate a string containing some
     * function. It can be used as well for a piece of code using return
     */
    @SuppressWarnings({ "rawtypes", "null" })
    public Object apply(String source, int lineNo, int columnNo,
            Object funcBody, Vector paramNames, Vector arguments)
            throws BSFException {
        String script = funcBody.toString();
        Object retval = null;
        try {
            // Fesi unwrap the returned object if needed
            int nParam = (paramNames == null) ? 0 : paramNames.size();
            String[] names = new String[nParam];
            Object[] values = new Object[nParam];
            for (int i = 0; i < nParam; i++) {
                names[i] = (String) paramNames.get(i);
                values[i] = arguments.get(i);
            }
            retval = global.evalAsFunction(script, names, values);
        } catch (Throwable t) { // includes JavaScriptException, rethrows Errors
            handleError(t);
        }
        return retval;
    }

    /**
     * Return an object from an extension - for compatibility with Rhino.
     * 
     * @param object
     *            Object on which to make the call.
     * @param method
     *            The name of the method to call.
     * @param args
     *            an array of arguments to be passed to the extension, which may
     *            be either Vectors of Nodes, or Strings.
     */
    public Object call(Object object, String method, Object[] args)
            throws BSFException {
        Object theReturnValue = null;

        JSObject target = global; // by default
        if (object != null) {
            if (!(object instanceof JSObject)) {
                throw new BSFException("Tartget object not a JSOBject");
            }
            target = (JSObject) object;
        }

        try {
            // Fesi unwrap the returned object if needed
            theReturnValue = target.call(method, args);
        } catch (Throwable t) {
            handleError(t);
        }
        return theReturnValue;
    }

    public void declareBean(BSFDeclaredBean bean) throws BSFException {
        try {
            global.setMember(bean.name, bean.bean);
        } catch (Throwable t) {
            handleError(t);
        }
    }

    public void undeclareBean(BSFDeclaredBean bean) throws BSFException {
        try {
            global.removeMember(bean.name);
        } catch (Throwable t) {
            handleError(t);
        }
    }

    private void handleError(Throwable t) throws BSFException {
        // if (t instanceof WrappedException) {
        // t = (Throwable)((WrappedException)t).unwrap();
        // }

        String message = null;
        Throwable target = t;

        if (t instanceof JSException) {
            message = t.getLocalizedMessage();

            // // Is it an exception wrapped in a JavaScriptException?
            // Object value = ((JavaScriptException)t).getValue();
            // if (value instanceof Throwable) {
            // // likely a wrapped exception from a LiveConnect call.
            // // Display its stack trace as a diagnostic
            // target = (Throwable)value;
            // }
            // } else if (t instanceof EvaluatorException
            // || t instanceof SecurityException) {
            // message = t.getLocalizedMessage();
        } else if (t instanceof RuntimeException) {
            message = "Internal Error: " + t.toString();
        } else if (t instanceof StackOverflowError) {
            message = "Stack Overflow";
        }

        if (message == null) {
            message = t.toString();
        }

        // REMIND: can we recover the line number here? I think
        // Rhino does this by looking up the stack for bytecode
        // see Context.getSourcePositionFromStack()
        // but I don't think this would work in interpreted mode

        if (t instanceof Error && !(t instanceof StackOverflowError)) {
            // Re-throw Errors because we're supposed to let the JVM see it
            // Don't re-throw StackOverflows, because we know we've
            // corrected the situation by aborting the loop and
            // a long stacktrace would end up on the user's console
            throw (Error) t;
        }
        throw new BSFException(BSFException.REASON_OTHER_ERROR,
                "EcmaScript Error: " + message, target);
    }

    /**
     * Return the global object of the engine
     * 
     * @return the global object of the engine
     */
    public JSGlobalObject getJSGlobalObject() {
        return global;
    }

}