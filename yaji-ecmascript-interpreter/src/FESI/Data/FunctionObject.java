// FunctionObject.java
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

package FESI.Data;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.yaji.log.ILog;
import org.yaji.log.Logs;

import FESI.AST.ASTFormalParameterList;
import FESI.AST.ASTStatementList;
import FESI.AST.EcmaScriptTreeConstants;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.EcmaScriptParseException;
import FESI.Exceptions.ProgrammingError;
import FESI.Exceptions.SyntaxError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.EcmaScriptVariableVisitor;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.FunctionEvaluationSource;
import FESI.Interpreter.StringEvaluationSource;
import FESI.Parser.EcmaScript;
import FESI.Parser.ParseException;
import FESI.Parser.StrictMode;

/**
 * Implements the EcmaScript Function singleton
 */
public class FunctionObject extends BuiltinFunctionObject implements
        EcmaScriptTreeConstants {
    private static final long serialVersionUID = 8501827292633127950L;
    private static final ILog log = Logs.getLog(FunctionObject.class);
    
    static boolean debugParse = false;
    // For functionPrototype
    private static class FunctionPrototypeToString extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        FunctionPrototypeToString(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            if (! (thisObject instanceof FunctionPrototype) ){
                throw new TypeError("Cannot apply Function.prototype.toString to non-function object");
            }
            String s = "function "
                    + ((FunctionPrototype) thisObject)
                            .getFunctionName()
                    + ((FunctionPrototype) thisObject)
                            .getFunctionParametersString()
                    + ((FunctionPrototype) thisObject)
                            .getFunctionImplementationString();
            return new ESString(s);
        }
    }
    
    private static class FunctionPrototypeBind extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;
        FunctionPrototypeBind(String name, Evaluator evaluator, FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisValue, ESValue[] arguments) throws EcmaScriptException {
            if (!thisValue.isCallable()) {
                throw new TypeError("bind must be called on a Function");
            }
            ESObject target = thisValue.toESObject(getEvaluator());
            int length = target.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash).toInt32();
            ESValue[] boundArgs;
            if (arguments.length > 1) {
                int extraArgumentsLength = arguments.length - 1;
                length = Math.max(0, length - extraArgumentsLength);
                boundArgs = new ESValue[extraArgumentsLength];
                System.arraycopy(arguments, 1, boundArgs, 0, extraArgumentsLength);
            } else {
                boundArgs = ESValue.EMPTY_ARRAY;
            }
            FunctionPrototype function = new BoundFunctionPrototype(getEvaluator().getFunctionPrototype(), getEvaluator(), length, target, getArg(arguments,0), boundArgs);
            return function;
        }
        
    }
    
    private static class FunctionPrototypeCall extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;
        FunctionPrototypeCall(String name, Evaluator evaluator, FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            ESValue[] functionArguments;
            ESValue target;
            if (arguments.length == 0) {
                functionArguments = ESValue.EMPTY_ARRAY;
                target = ESUndefined.theUndefined;
            } else {
                functionArguments = new ESValue[arguments.length-1];
                System.arraycopy(arguments, 1, functionArguments, 0, functionArguments.length);
                target = arguments[0];
            }
            return thisObject.callFunction(target,functionArguments);
        }
        
    }
    
    private static class FunctionPrototypeApply extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;
        FunctionPrototypeApply(String name, Evaluator evaluator, FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 2);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            ESValue[] functionArguments = ESValue.EMPTY_ARRAY;
            ESValue target;
            if (arguments.length == 0) {
                target = ESUndefined.theUndefined;
            } else if (arguments.length == 1) {
                target = arguments[0];
            } else {
                target = arguments[0];
                ESValue arrayValue = arguments[1];
                if (arrayValue == ESUndefined.theUndefined || arrayValue == ESNull.theNull) {
                    functionArguments = ESValue.EMPTY_ARRAY;
                } else {
                    if (!(arrayValue instanceof ESObject)) {
                        throw new TypeError("Second parameter 'argArray' supplied to 'apply' must be an object");
                    }
                    ESObject array = (ESObject) arrayValue;
                    int length = array.getProperty(StandardProperty.LENGTHstring,StandardProperty.LENGTHhash).toInt32();
                    functionArguments = new ESValue[length];
                    for( int index=0; index<length; index++) {
                        functionArguments[index] = array.getProperty((long)index);
                    }
                }
            }
            return thisObject.callFunction(target,functionArguments);
        }
        
    }
    

    FunctionObject(FunctionPrototype prototype, Evaluator evaluator) throws EcmaScriptException {
        super(prototype, evaluator, "Function", 1);
        putProperty(StandardProperty.PROTOTYPEstring, 0, prototype);
        prototype.putHiddenProperty("constructor", this);
        prototype.putHiddenProperty("toString",
                new FunctionPrototypeToString("toString", evaluator, prototype));
        prototype.putHiddenProperty("call", new FunctionPrototypeCall("call", evaluator, prototype));
        prototype.putHiddenProperty("apply", new FunctionPrototypeApply("apply", evaluator, prototype));
        prototype.putHiddenProperty("bind", new FunctionPrototypeBind("bind", evaluator, prototype));
    }

    // overrides
    @Override
    public String getESClassName() {
        return "Function";
    }

    // overrides - call and new have the same effect
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        return doConstruct(arguments);
    }

    private static class CloseableStringReader extends StringReader {

        private boolean isClosed;

        public CloseableStringReader(String trimmedParams) {
            super(trimmedParams);
        }
        
        @Override
        public void close() {
            super.close();
            isClosed = true;
        }
        
        public boolean isClosed() {
            return isClosed;
        }
    }
    // overrides - build a new function
    @Override
    public ESObject doConstruct(ESValue[] arguments)
            throws EcmaScriptException {
        ConstructedFunctionObject theFunction = null;
        ASTFormalParameterList fpl = null;
        ASTStatementList sl = null;

        StringBuilder parameters = new StringBuilder();
        int nArgs = arguments.length;
        int i;
        for (i = 0; i < (nArgs - 1); i++) {
            if (i > 0) {
                parameters.append(',');
            }
            String arg = arguments[i].callToString();
            parameters.append(arg);
        }
        String body = nArgs == 0 ? "" : arguments[i].callToString();

        String trimmedParams = parameters.toString().trim();

        String fullFunctionText = "function anonymous (" + trimmedParams
                + ") {" + body.toString() + "}";

        EcmaScript parser;

        // Special case for empty parameters
        if (trimmedParams.length() == 0) {
            fpl = new ASTFormalParameterList(JJTFORMALPARAMETERLIST);
        } else {
            CloseableStringReader is = new CloseableStringReader(trimmedParams);
            parser = new EcmaScript(is);
            try {
                fpl = (ASTFormalParameterList) parser.FormalParameterList();
                
                if (!is.isClosed() && is.ready()) {
                    throw new SyntaxError("All arguments passed to function constructor could not be parsed as parameters");
                }
            } catch (ParseException e) {
                if (debugParse) {
                    log.asError("[[PARSING ERROR DETECTED: (debugParse true)]]", e);
                }
                throw new EcmaScriptParseException(e,
                        new StringEvaluationSource(fullFunctionText, null));
            } catch (IOException e) {
                throw new ProgrammingError("Unexpected IOException parsing arguments for function");
            } finally {
                is.close();
            }
        }
        if (body.length() > 0) {
            StringReader is = new java.io.StringReader(body.toString());
            parser = new EcmaScript(is);
            try {
                sl = (ASTStatementList) parser.StatementList();
                is.close();
            } catch (ParseException e) {
                if (debugParse) {
                    log.asError("[[PARSING ERROR DETECTED: (debugParse true)]]", e);
                }
                throw new EcmaScriptParseException(e, new StringEvaluationSource(
                        fullFunctionText, null));
            }
        } else {
            sl = new ASTStatementList(0);
        }
        FunctionEvaluationSource fes = new FunctionEvaluationSource(
                new StringEvaluationSource(fullFunctionText, null),
                "<anonymous function>");
        EcmaScriptVariableVisitor varDeclarationVisitor = getEvaluator()
                .getVarDeclarationVisitor();
        List<String> variableNames = varDeclarationVisitor
                .processVariableDeclarations(sl, fes);

        theFunction = ConstructedFunctionObject.makeNewConstructedFunction(
                getEvaluator(), "anonymous", fes, fullFunctionText, fpl
                        .getArguments(), variableNames, sl, null,StrictMode.hasStrictModeDirective(sl));

        return theFunction;
    }

    // overrides
    @Override
    public String toString() {
        return "<Function>";
    }
    
    
}
