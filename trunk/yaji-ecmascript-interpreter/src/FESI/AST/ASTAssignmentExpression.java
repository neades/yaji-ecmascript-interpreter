/* Generated By:JJTree: Do not edit this line. ASTAssignmentExpression.java */

package FESI.AST;

import FESI.Parser.EcmaScript;

public class ASTAssignmentExpression extends SimpleNode {
    /**
	 * 
	 */
    private static final long serialVersionUID = -24694765230410633L;

    public ASTAssignmentExpression(int id) {
        super(id);
    }

    public ASTAssignmentExpression(EcmaScript p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTAssignmentExpression(id);
    }

    public static Node jjtCreate(EcmaScript p, int id) {
        return new ASTAssignmentExpression(p, id);
    }

    /** Accept the visitor. **/
    @Override
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}