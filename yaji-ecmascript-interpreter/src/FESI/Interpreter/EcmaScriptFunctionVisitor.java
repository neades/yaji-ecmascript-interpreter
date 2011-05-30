// EcmaScriptFunctionVisitor.java
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

import FESI.AST.ASTAllocationExpression;
import FESI.AST.ASTAndExpressionSequence;
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
import FESI.Data.ConstructedFunctionObject;
import FESI.Data.ESReference;
import FESI.Data.GlobalObject;
import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Parser.EcmaScriptConstants;

/**
 * The function visitor use the visitor pattern to iterate the parsed code. It
 * examine all function declarations and assign them to their global variable.
 * <P>
 * The function declarations will be ignored by the evaluation visitor (the tree
 * is not modified).
 */
public class EcmaScriptFunctionVisitor implements EcmaScriptVisitor,
        EcmaScriptConstants, java.io.Serializable {
    private static final long serialVersionUID = 4746752174336980575L;
    // The visitor work on behalf on an evaluator
    private Evaluator evaluator = null;
    private boolean debug = false;
    private EvaluationSource currentEvaluationSource = null;

    /**
     * Create a new visitor
     * 
     * @param evaluator
     *            On behalf of this evaluator
     */
    public EcmaScriptFunctionVisitor(Evaluator evaluator) {
        super();
        this.evaluator = evaluator;
    }

    /**
     * Process all function declarations in ths parse tree
     * 
     * @param node
     *            The parse tree
     * @evaluationSource A description of the source for error messages
     */
    public void processFunctionDeclarations(ASTProgram node,
            EvaluationSource evaluationSource) {
        if (debug)
            System.out.println("processFunctionDeclarations: " + node);
        if (currentEvaluationSource != null)
            throw new ProgrammingError("illegal recursive function definition");
        this.currentEvaluationSource = evaluationSource;
        try {
            node.jjtAccept(this, null);
        } finally {
            this.currentEvaluationSource = null;
        }
    }

    /*--------------------------------------------------------------------
     * The following routines implement the walking process
     * Irrelevant parts of the tree are skipped
     *------------------------------------------------------------------*/

    private void badAST() {
        throw new ProgrammingError("Bad AST walk in EcmaScriptFunctionVisitor");
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
        badAST();
        return data;
    }

    public Object visit(ASTFunctionDeclaration node, Object data) {
        int nChildren = node.jjtGetNumChildren();
        if (nChildren != 3) {
            throw new ProgrammingError("Bad AST in function declaration");
        }

        ASTIdentifier idNode = (ASTIdentifier) (node.jjtGetChild(0));
        FunctionEvaluationSource fes = new FunctionEvaluationSource(
                currentEvaluationSource, idNode.getName());

        ASTFormalParameterList fpl = (ASTFormalParameterList) (node
                .jjtGetChild(1));
        ASTStatementList sl = (ASTStatementList) (node.jjtGetChild(2));
        EcmaScriptVariableVisitor varDeclarationVisitor = evaluator
                .getVarDeclarationVisitor();
        List<String> variableNames = varDeclarationVisitor
                .processVariableDeclarations(sl, fes);

        if (debug)
            System.out.println("FUNC DECL: " + idNode.getName());
        GlobalObject go = evaluator.getGlobalObject();
        try {
            ESReference newVar = new ESReference(go, idNode.getName(), idNode
                    .hashCode());
            ConstructedFunctionObject func = ConstructedFunctionObject
                    .makeNewConstructedFunction(evaluator, idNode.getName(),
                            fes, node.getSourceString(), fpl.getArguments(),
                            variableNames, sl);
            evaluator.putValue(newVar, func);
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError("Unexpected error registering function"
                    + e.getMessage());
        }
        return data;
    }

    public Object visit(ASTFormalParameterList node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTStatement node, Object data) {
        // Ignore statements for function declaration visitors
        return data;
    }

    public Object visit(ASTVariableDeclaration node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTIfStatement node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTContinueStatement node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTWhileStatement node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTForStatement node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTForInStatement node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTForVarStatement node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTForVarInStatement node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTBreakStatement node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTReturnStatement node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTWithStatement node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTThisReference node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTSuperReference node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTCompositeReference node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTFunctionCallParameters node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTPropertyValueReference node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTPropertyIdentifierReference node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTAllocationExpression node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTOperator node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTPostfixExpression node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTUnaryExpression node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTBinaryExpressionSequence node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTAndExpressionSequence node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTOrExpressionSequence node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTConditionalExpression node, Object data) {
        badAST();
        return data;
    }

    // Can we really have a cascade ?
    public Object visit(ASTAssignmentExpression node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTExpressionList node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTEmptyExpression node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTLiteral node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTIdentifier node, Object data) {
        badAST();
        return data;
    }

    public Object visit(ASTObjectLiteral node, Object data) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object visit(ASTPropertyNameAndValue node, Object data) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object visit(ASTFunctionExpression node, Object data) {
        // TODO Auto-generated method stub
        return null;
    }

}