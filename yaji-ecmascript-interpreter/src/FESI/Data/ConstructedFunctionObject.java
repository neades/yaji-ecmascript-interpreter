// ConstructedFunctionObject.java
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

import java.util.HashSet;
import java.util.List;

import FESI.AST.ASTStatementList;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.SyntaxError;
import FESI.Interpreter.EvaluationSource;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ScopeChain;
import FESI.Parser.StrictMode;

/**
 * Implements functions constructed from source text
 */
public class ConstructedFunctionObject extends FunctionPrototype {
    private static final long serialVersionUID = 8665440834402496188L;

    private ASTStatementList theFunctionAST;
    private String[] theArguments;
    private List<String> localVariableNames;
    private EvaluationSource evaluationSource = null;
    private String functionSource = null;

    private ESValue currentArguments = ESNull.theNull;
    private final ScopeChain scopeChain;
    private final boolean isStrictMode;

    private ConstructedFunctionObject(FunctionPrototype functionPrototype,
            Evaluator evaluator, String functionName,
            EvaluationSource evaluationSource, String functionSource,
            String[] arguments, List<String> localVariableNames,
            ASTStatementList aFunctionAST, ScopeChain scopeChain, boolean isStrictMode) throws EcmaScriptException {
        super(functionPrototype, evaluator, functionName, arguments.length);
        this.evaluationSource = evaluationSource;
        this.functionSource = functionSource;
        theFunctionAST = aFunctionAST;
        theArguments = arguments;
        this.localVariableNames = localVariableNames;
        this.scopeChain = scopeChain;
        this.isStrictMode = isStrictMode;

        if (isStrictMode) {
            validateStrictMode(theArguments);
        }
        // try {
        // targetObject.putProperty(functionName, this);
        // } catch (EcmaScriptException e) {
        // throw new ProgrammingError(e.getMessage());
        // }
    }

    private void validateStrictMode(String[] formalParameterNames) throws EcmaScriptException {
        HashSet<String> existing = new HashSet<String>();
        for (String formalParameterName : formalParameterNames) {
            if (formalParameterName.equals(StandardProperty.ARGUMENTSstring) || formalParameterName.equals(StandardProperty.EVALstring)) {
                throw new SyntaxError(StrictMode.EXCEPTION_PREFIX+formalParameterName+" cannot be used as a parameter name");
            }
            if (existing.contains(formalParameterName)) {
                throw new SyntaxError(StrictMode.EXCEPTION_PREFIX+formalParameterName+" cannot be repeated in formal parameter list");
            }
            existing.add(formalParameterName);
        }
    }

    /**
     * get the string defining the function
     * 
     * @return the source string
     */
    @Override
    public String getFunctionImplementationString() {
        if (functionSource == null) {
            StringBuilder str = new StringBuilder();
            str.append("function ");
            str.append(getFunctionName());
            str.append('(');
            for (int i = 0; i < theArguments.length; i++) {
                if (i > 0) {
                    str.append(',');
                }
                str.append(theArguments[i]);
            }
            str.append(')');
            str.append("function {<internal abstract syntax tree representation>}");
            return str.toString();
        }
        return functionSource;

    }

    /**
     * Get the list of local variables of the function as a vector
     * 
     * @return the Vector of local variable name strings
     */
    public List<String> getLocalVariableNames() {
        return localVariableNames;
    }

    /**
     * Get the function parameter description as a string
     * 
     * @return the function parameter string as (a,b,c)
     */
    @Override
    public String getFunctionParametersString() {
        StringBuilder str = new StringBuilder();
        str.append('(');
        for (int i = 0; i < theArguments.length; i++) {
            if (i > 0)
                str.append(',');
            str.append(theArguments[i]);
        }
        str.append(')');
        return str.toString();
    }

    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        ESValue value = null;
        Evaluator evaluator = getEvaluator();
        boolean strictMode = evaluator.isStrictMode();
        ESArguments args = ESArguments.makeNewESArguments(evaluator, this, theArguments, arguments);
        ESValue oldArguments = currentArguments;
        currentArguments = args;
        try {
            evaluator.setStrictMode(strictMode || isStrictMode);
            value = getEvaluator().evaluateFunctionInScope(theFunctionAST, evaluationSource, args, localVariableNames, thisObject, scopeChain);
        } finally {
            currentArguments = oldArguments;
            evaluator.setStrictMode(strictMode);
        }
        return value;
    }

    @Override
    public ESObject doConstruct(ESValue[] arguments)
            throws EcmaScriptException {
        ESValue prototype = getProperty(StandardProperty.PROTOTYPEstring, StandardProperty.PROTOTYPEhash);
        Evaluator evaluator = getEvaluator();
        ESObject op = evaluator.getObjectPrototype();
        if (!(prototype instanceof ESObject))
            prototype = op;
        ESObject obj = new ObjectPrototype((ESObject) prototype, getEvaluator());
        ESValue result = callFunction(obj, arguments);
        if (result instanceof ESObject) {
            ESObject object = (ESObject) result;
            if (object.getTypeOf() == EStypeObject) {
                obj = (ESObject) result;
            }
        } 
        return obj;
    }

    @Override
    public String toString() {
        return getFunctionImplementationString();
    }

    @Override
    public String toDetailString() {
        StringBuilder str = new StringBuilder();
        str.append("<Function: ");
        str.append(getFunctionName());
        str.append('(');
        for (int i = 0; i < theArguments.length; i++) {
            if (i > 0)
                str.append(',');
            str.append(theArguments[i]);
        }
        str.append(")>");
        return str.toString();
    }

    /**
     * Utility function to create a function object. Used by the EcmaScript
     * Function function to create new functions
     * 
     * @param evaluator
     *            the Evaluator
     * @param functionName
     *            the name of the new function
     * @param evaluationSource
     *            An identification of the source of the function
     * @param sourceString
     *            The source of the parsed function
     * @param arguments
     *            The array of arguments
     * @param localVariableNames
     *            the list of local variable declared by var
     * @param aFunctionAST
     *            the parsed function
     * @param scopeChain 
     *            the current Scope Chain for Function Expressions - null for Function Declarations
     * @param isStrictMode 
     *            indication if function is in strict mode
     * @return A new function object
     * @throws EcmaScriptException 
     */
    public static ConstructedFunctionObject makeNewConstructedFunction(
            Evaluator evaluator, String functionName,
            EvaluationSource evaluationSource, String sourceString,
            String[] arguments, List<String> localVariableNames,
            ASTStatementList aFunctionAST, ScopeChain scopeChain, boolean isStrictMode) throws EcmaScriptException {

        FunctionPrototype fp = (FunctionPrototype) evaluator
                .getFunctionPrototype();

        ConstructedFunctionObject theNewFunction = null;
        theNewFunction = new ConstructedFunctionObject(fp, evaluator,
                functionName, evaluationSource, sourceString, arguments,
                localVariableNames, aFunctionAST, scopeChain, isStrictMode);
        ObjectPrototype thePrototype = ObjectObject.createObject(evaluator);
        theNewFunction.putProperty(StandardProperty.PROTOTYPEstring, WRITEABLE, thePrototype);
        thePrototype.putHiddenProperty("constructor", theNewFunction);
        return theNewFunction;
    }

    @Override
    public ESValue getPropertyInScope(String propertyName,
            ScopeChain previousScope, int hash) throws EcmaScriptException {
        if (hash == StandardProperty.ARGUMENTShash && propertyName.equals(StandardProperty.ARGUMENTSstring)) {
            return currentArguments;
        }
        return super.getPropertyInScope(propertyName, previousScope, hash);

    }

    @Override
    public ESValue getPropertyIfAvailable(String propertyName, int hash)
            throws EcmaScriptException {
        if (hash == StandardProperty.ARGUMENTShash && propertyName.equals(StandardProperty.ARGUMENTSstring)) {
            return currentArguments;
        }
        return super.getPropertyIfAvailable(propertyName, hash);

    }

    // overrides
    @Override
    public void putProperty(String propertyName, ESValue propertyValue, int hash)
            throws EcmaScriptException {
        if (hash != StandardProperty.ARGUMENTShash || ! propertyName.equals(StandardProperty.ARGUMENTSstring)) {
            super.putProperty(propertyName, propertyValue, hash);
        } // Allowed via putHiddenProperty, used internally !
    }

    // public ESValue replaceCurrentArguments(ESObject newArguments) {
    // ESValue oldArguments = currentArguments;
    // currentArguments = newArguments;
    // return oldArguments;
    // }

}
