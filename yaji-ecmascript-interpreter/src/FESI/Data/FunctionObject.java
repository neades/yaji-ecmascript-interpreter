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

    FunctionObject(ESObject prototype, Evaluator evaluator) throws EcmaScriptException {
        super(prototype, evaluator, "Function", 1);
        putHiddenProperty("prototype", evaluator.getFunctionPrototype());
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
