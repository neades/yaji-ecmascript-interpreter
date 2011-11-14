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

import FESI.AST.ASTFormalParameterList;
import FESI.AST.ASTFunctionDeclaration;
import FESI.AST.ASTIdentifier;
import FESI.AST.ASTProgram;
import FESI.AST.ASTStatement;
import FESI.AST.ASTStatementList;
import FESI.AST.AbstractEcmaScriptVisitor;
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
public class EcmaScriptFunctionVisitor extends AbstractEcmaScriptVisitor implements EcmaScriptConstants, java.io.Serializable {
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

    @Override
    public Object defaultAction(SimpleNode node, Object data) {
        throw new ProgrammingError("Bad AST walk in EcmaScriptFunctionVisitor");
    }

    @Override
    public Object visit(ASTProgram node, Object data) {
        data = node.childrenAccept(this, data);
        return data;
    }

    @Override
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
                            variableNames, sl, null, node.isStrictMode());
            evaluator.putValue(newVar, func);
        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError("Unexpected error registering function"
                    + e.getMessage());
        }
        return data;
    }

     @Override
    public Object visit(ASTStatement node, Object data) {
        // Ignore statements for function declaration visitors
        return data;
    }

}