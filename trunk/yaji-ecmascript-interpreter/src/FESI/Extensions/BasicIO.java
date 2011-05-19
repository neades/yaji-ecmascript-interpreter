// BasicIO.java
// FESI Copyright (c) Jean-Marc Lugrin, 1999
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package FESI.Extensions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import FESI.Data.BuiltinFunctionObject;
import FESI.Data.ESBoolean;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.FunctionPrototype;
import FESI.Data.GlobalObject;
import FESI.Data.ObjectObject;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.EcmaScriptParseException;
import FESI.Interpreter.Evaluator;

/**
 * Implements basic input/out capabilities for console based (stdin/stdout)
 * applications.
 * <P>
 * The few functions allow minimal communication with the user (including
 * displaying error messages and prompting) in a way portable to windowing
 * environments.
 */
public class BasicIO extends Extension implements BasicIOInterface {

    // Implement the EcmaScript functions
    private static final long serialVersionUID = 8886886834315148679L;

    class GlobalObjectWrite extends BuiltinFunctionObject {
        private static final long serialVersionUID = 7397963994845332069L;

        GlobalObjectWrite(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            for (int i = 0; i < arguments.length; i++) {
                System.out.print(arguments[i].toString());
            }
            return ESUndefined.theUndefined;
        }
    }

    class GlobalObjectWriteln extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1049266936209327085L;

        GlobalObjectWriteln(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            for (int i = 0; i < arguments.length; i++) {
                System.out.print(arguments[i].toString());
            }
            System.out.println();
            return ESUndefined.theUndefined;
        }
    }

    class GlobalObjectAlert extends BuiltinFunctionObject {
        private static final long serialVersionUID = -6826063586063912091L;

        GlobalObjectAlert(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            System.err.print("[[ALERT]] ");
            for (int i = 0; i < arguments.length; i++) {
                System.err.print(arguments[i].toString());
            }
            System.err.println();
            return ESUndefined.theUndefined;
        }
    }

    class GlobalObjectPrompt extends BuiltinFunctionObject {
        private static final long serialVersionUID = -2335657696435213349L;

        GlobalObjectPrompt(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 2);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            String defaultValue = "";
            String theQuery = "?";
            if (arguments.length > 0)
                theQuery = arguments[0].toString();
            if (arguments.length > 1)
                defaultValue = arguments[1].toString();
            System.out.print(theQuery + " [" + defaultValue + "] ? ");
            System.out.flush();
            String response = null;
            try {
                response = (new BufferedReader(new InputStreamReader(System.in)))
                        .readLine();
            } catch (IOException e) {
                // response = null;
            }
            if (response == null || response.equals(""))
                response = defaultValue;
            return new ESString(response);
        }
    }

    class GlobalObjectConfirm extends BuiltinFunctionObject {
        private static final long serialVersionUID = -7656437588299476127L;

        GlobalObjectConfirm(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 2);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            String theQuery = "?";
            if (arguments.length > 0)
                theQuery = arguments[0].toString();
            System.out.print(theQuery + " [y/n] ? ");
            System.out.flush();
            String response = null;
            try {
                response = (new BufferedReader(new InputStreamReader(System.in)))
                        .readLine();
            } catch (IOException e) {
                // response = null;
            }
            if (response != null
                    & response.trim().toLowerCase().startsWith("y")) {
                return ESBoolean.makeBoolean(true);
            }
            return ESBoolean.makeBoolean(false);
        }
    }

    class GlobalObjectExit extends BuiltinFunctionObject {
        private static final long serialVersionUID = 2847240928500692081L;

        GlobalObjectExit(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            int status = 0;
            if (arguments.length > 0) {
                status = arguments[0].toInt32();
            }

            System.exit(status);
            return null; // Never reached
        }
    }

    class GlobalObjectNoop extends BuiltinFunctionObject {
        private static final long serialVersionUID = 625259366460316597L;

        GlobalObjectNoop(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0); // 0 = Just some default value
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {

            return ESUndefined.theUndefined;
        }
    }

    class GlobalObjectLoad extends BuiltinFunctionObject {
        private static final long serialVersionUID = 7206069117646107995L;

        GlobalObjectLoad(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1); // 0 = Just some default value
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            String fileName = null;
            if (arguments.length > 0)
                fileName = arguments[0].toString();
            if (fileName == null)
                throw new EcmaScriptException("Missing file name for load");
            ESValue value;
            try {
                value = this.getEvaluator().evaluateLoadModule(fileName);
            } catch (EcmaScriptParseException e) {
                e.setNeverIncomplete();
                throw e;
            }
            return value;
        }

    }

    private ESObject document = null;
    private ESObject window = null;

    /**
     * Create a new instance of the BasicIO extension
     */
    public BasicIO() {
        super();
    }

    // implements BasicIOInterface
    public ESObject getDocument() {
        return document;
    }

    /**
     * Initialize the extension
     */
    public void initializeExtension(Evaluator evaluator)
            throws EcmaScriptException {

        GlobalObject go = evaluator.getGlobalObject();
        document = ObjectObject.createObject(evaluator);
        window = ObjectObject.createObject(evaluator);
        FunctionPrototype fp = (FunctionPrototype) evaluator
                .getFunctionPrototype();

        go.putHiddenProperty("document", document);
        document.putHiddenProperty("write", new GlobalObjectWrite("write",
                evaluator, fp));
        document.putHiddenProperty("writeln", new GlobalObjectWriteln(
                "writeln", evaluator, fp));
        document.putHiddenProperty("open", new GlobalObjectNoop("open",
                evaluator, fp));
        document.putHiddenProperty("close", new GlobalObjectNoop("close",
                evaluator, fp));
        document.putHiddenProperty("URL", new ESString("file://<unknown>"));

        go.putHiddenProperty("window", window);
        window.putHiddenProperty("alert", new GlobalObjectAlert("alert",
                evaluator, fp));
        window.putHiddenProperty("prompt", new GlobalObjectPrompt("prompt",
                evaluator, fp));
        window.putHiddenProperty("confirm", new GlobalObjectConfirm("confirm",
                evaluator, fp));

        go.putHiddenProperty("write", new GlobalObjectWrite("write", evaluator,
                fp));
        go.putHiddenProperty("writeln", new GlobalObjectWriteln("writeln",
                evaluator, fp));
        go.putHiddenProperty("alert", new GlobalObjectAlert("alert", evaluator,
                fp));
        go.putHiddenProperty("prompt", new GlobalObjectPrompt("prompt",
                evaluator, fp));
        go.putHiddenProperty("confirm", new GlobalObjectConfirm("confirm",
                evaluator, fp));
        go.putHiddenProperty("exit",
                new GlobalObjectExit("exit", evaluator, fp));
        go.putHiddenProperty("load",
                new GlobalObjectLoad("load", evaluator, fp));
    }
}
