// EcmaScriptVariableVisitor.java
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

package FESI.Interpreter;

import java.util.List;
import java.util.Vector;

import FESI.AST.ASTAllocationExpression;
import FESI.AST.ASTAndExpressionSequence;
import FESI.AST.ASTArrayLiteral;
import FESI.AST.ASTAssignmentExpression;
import FESI.AST.ASTBinaryExpressionSequence;
import FESI.AST.ASTBreakStatement;
import FESI.AST.ASTCompositeReference;
import FESI.AST.ASTConditionalExpression;
import FESI.AST.ASTContinueStatement;
import FESI.AST.ASTEmptyExpression;
import FESI.AST.ASTExpressionList;
import FESI.AST.ASTForInStatement;
import FESI.AST.ASTForStatement;
import FESI.AST.ASTForVarInStatement;
import FESI.AST.ASTForVarStatement;
import FESI.AST.ASTFormalParameterList;
import FESI.AST.ASTFunctionCallParameters;
import FESI.AST.ASTFunctionDeclaration;
import FESI.AST.ASTFunctionExpression;
import FESI.AST.ASTIdentifier;
import FESI.AST.ASTIfStatement;
import FESI.AST.ASTLiteral;
import FESI.AST.ASTObjectLiteral;
import FESI.AST.ASTOperator;
import FESI.AST.ASTOrExpressionSequence;
import FESI.AST.ASTPostfixExpression;
import FESI.AST.ASTProgram;
import FESI.AST.ASTPropertyIdentifierReference;
import FESI.AST.ASTPropertyNameAndValue;
import FESI.AST.ASTPropertyValueReference;
import FESI.AST.ASTReturnStatement;
import FESI.AST.ASTStatement;
import FESI.AST.ASTStatementList;
import FESI.AST.ASTSuperReference;
import FESI.AST.ASTThisReference;
import FESI.AST.ASTUnaryExpression;
import FESI.AST.ASTVariableDeclaration;
import FESI.AST.ASTWhileStatement;
import FESI.AST.ASTWithStatement;
import FESI.AST.EcmaScriptVisitor;
import FESI.AST.SimpleNode;
import FESI.Exceptions.ProgrammingError;
import FESI.Parser.EcmaScriptConstants;

/**
 * The variable visitor use the visitor pattern to iterate the parsed code. It
 * examine all variable declaration at the current level (it does not recurse in
 * functions) and return the list of variables as a vector.
 * <P>
 * The variable declarations will be ignored by the evaluation visitor (the tree
 * is not modified).
 */
public class EcmaScriptVariableVisitor implements EcmaScriptVisitor,
        EcmaScriptConstants, java.io.Serializable {
    private static final long serialVersionUID = 989146122497439912L;
    private boolean debug = false;
    private Vector<String> variableList = null;

    /**
     * Create a new visitor
     */
    public EcmaScriptVariableVisitor() {
        super();
    }

    /**
     * Process all variable declarations at the global level
     * 
     * @param node
     *            The parse tree
     * @evaluationSource A description of the source for error messages
     * @return A vector of variables
     */
    public List<String> processVariableDeclarations(ASTProgram node,
            EvaluationSource evaluationSource) {
        if (debug)
            System.out.println("processVariableDeclarations for program: "
                    + node);
        variableList = new Vector<String>();
        node.jjtAccept(this, evaluationSource);
        return variableList;
    }

    /**
     * Process all variable declarations at the statement list level
     * 
     * @param node
     *            The parse tree
     * @evaluationSource A description of the source for error messages
     * @return A vector of variables
     */
    public List<String> processVariableDeclarations(ASTStatementList node,
            EvaluationSource evaluationSource) {
        if (debug)
            System.out
                    .println("processVariableDeclarations for function body: "
                            + node);
        variableList = new Vector<String>();
        node.jjtAccept(this, evaluationSource);
        return variableList;
    }

    /*--------------------------------------------------------------------
     * The following routines implement the walking process
     * Irrelevant parts of the tree are skipped
     *------------------------------------------------------------------*/

    private void badAST() {
        throw new ProgrammingError("Bad AST walk in EcmaScriptVariableVisitor");
    }

    // The dispatching is by node type - if the specific visitor
    // is not implemented, then this routine is called
    public Object visit(SimpleNode node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTProgram node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTStatementList node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTFunctionDeclaration node, Object data) {
        // ignore function declarations in this mode
        return data;
    }

    public Object visit(ASTFormalParameterList node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTStatement node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTVariableDeclaration node, Object data) {
        int nChildren = node.jjtGetNumChildren();
        if (nChildren < 1 || nChildren > 2) {
            throw new ProgrammingError("Bad AST in variable declaration");
        }
        ASTIdentifier idNode = (ASTIdentifier) (node.jjtGetChild(0));
        if (debug)
            System.out.println("VAR DECL: " + idNode.getName());
        variableList.addElement(idNode.getName());
        // try {
        // evaluator.createVariable(idNode.getName());
        // } catch (EcmaScriptException e) {
        // e.printStackTrace();
        // throw new ProgrammingError(e.getMessage());
        // }
        return data;
    }

    public Object visit(ASTIfStatement node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTContinueStatement node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTWhileStatement node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTForStatement node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTForInStatement node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTForVarStatement node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTForVarInStatement node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTBreakStatement node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTReturnStatement node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTWithStatement node, Object data) {
        node.setEvaluationSource(data);
        data = node.childrenAccept(this, data);
        return data;
    }

    public Object visit(ASTThisReference node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTSuperReference node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTCompositeReference node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTFunctionCallParameters node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTPropertyValueReference node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTPropertyIdentifierReference node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTAllocationExpression node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTOperator node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTPostfixExpression node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTUnaryExpression node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTBinaryExpressionSequence node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTAndExpressionSequence node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTOrExpressionSequence node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTConditionalExpression node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    // Can we really have a cascade ?
    public Object visit(ASTAssignmentExpression node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTExpressionList node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTEmptyExpression node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTLiteral node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTIdentifier node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTObjectLiteral node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTPropertyNameAndValue node, Object data) {
        // no internal variable declarations possible
        return data;
    }

    public Object visit(ASTFunctionExpression node, Object data) {
        // Ignored in this mode
        return data;
    }

    public Object visit(ASTArrayLiteral node, Object data) {
        // no internal variable declarations possible
        return data;
    }
}