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

import java.util.List;

import FESI.AST.ASTFormalParameterList;
import FESI.AST.ASTStatementList;
import FESI.AST.EcmaScriptTreeConstants;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.EcmaScriptParseException;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.EcmaScriptVariableVisitor;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.FunctionEvaluationSource;
import FESI.Interpreter.StringEvaluationSource;
import FESI.Parser.EcmaScript;
import FESI.Parser.ParseException;

/**
 * Implements the EcmaScript Function singleton
 */
public class FunctionObject extends BuiltinFunctionObject implements
        EcmaScriptTreeConstants {
    private static final long serialVersionUID = 8501827292633127950L;
    static boolean debugParse = false;
    // For functionPrototype
    private static class FunctionPrototypeToString extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        FunctionPrototypeToString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
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
    
    private static class FunctionPrototypeCall extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;
        FunctionPrototypeCall(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

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
        FunctionPrototypeApply(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 2);
        }

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
                        functionArguments[index] = array.getProperty(index);
                    }
                }
            }
            return thisObject.callFunction(target,functionArguments);
        }
        
    }
    

    FunctionObject(FunctionPrototype prototype, Evaluator evaluator) throws EcmaScriptException {
        super(prototype, evaluator, "Function", 1);
        putHiddenProperty("prototype", prototype);
        prototype.putHiddenProperty("constructor", this);
        prototype.putHiddenProperty("toString",
                new FunctionPrototypeToString("toString", evaluator, prototype));
        prototype.putHiddenProperty("call", new FunctionPrototypeCall("call", evaluator, prototype));
        prototype.putHiddenProperty("apply", new FunctionPrototypeApply("apply", evaluator, prototype));
    }

    // overrides
    public String getESClassName() {
        return "Function";
    }

    // overrides - call and new have the same effect
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        return doConstruct(thisObject.toESObject(getEvaluator()), arguments);
    }

    // overrides - build a new function
    public ESObject doConstruct(ESObject thisObject, ESValue[] arguments)
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
            String arg = arguments[i].toString();
            parameters.append(arg);
        }
        String body = arguments[i].toString();

        String trimmedParams = parameters.toString().trim();

        String fullFunctionText = "function anonymous (" + trimmedParams
                + ") {" + body.toString() + "}";

        java.io.StringReader is;
        EcmaScript parser;

        // Special case for empty parameters
        if (trimmedParams.length() == 0) {
            fpl = new ASTFormalParameterList(JJTFORMALPARAMETERLIST);
        } else {
            is = new java.io.StringReader(trimmedParams);
            parser = new EcmaScript(is);
            try {
                fpl = (ASTFormalParameterList) parser.FormalParameterList();
                is.close();
            } catch (ParseException e) {
                if (debugParse) {
                    System.out
                            .println("[[PARSING ERROR DETECTED: (debugParse true)]]");
                    System.out.println(e.getMessage());
                    System.out.println("[[BY ROUTINE:]]");
                    e.printStackTrace();
                    System.out.println();
                }
                throw new EcmaScriptParseException(e,
                        new StringEvaluationSource(fullFunctionText, null));
            }
        }
        is = new java.io.StringReader(body.toString());
        parser = new EcmaScript(is);
        try {
            sl = (ASTStatementList) parser.StatementList();
            is.close();
        } catch (ParseException e) {
            if (debugParse) {
                System.out
                        .println("[[PARSING ERROR DETECTED: (debugParse true)]]");
                System.out.println(e.getMessage());
                System.out.println("[[BY ROUTINE:]]");
                e.printStackTrace();
                System.out.println();
            }
            throw new EcmaScriptParseException(e, new StringEvaluationSource(
                    fullFunctionText, null));
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
                        .getArguments(), variableNames, sl, null);

        return theFunction;
    }

    // overrides
    public String toString() {
        return "<Function>";
    }
    
    
}
