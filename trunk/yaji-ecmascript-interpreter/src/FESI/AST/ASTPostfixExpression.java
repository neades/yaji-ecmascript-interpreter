/* Generated By:JJTree: Do not edit this line. ASTPostfixExpression.java */

package FESI.AST;

import FESI.Parser.EcmaScript;

public class ASTPostfixExpression extends SimpleNode {
    /**
	 * 
	 */
    private static final long serialVersionUID = 8054866844983700093L;

    public ASTPostfixExpression(int id) {
        super(id);
    }

    public ASTPostfixExpression(EcmaScript p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPostfixExpression(id);
    }

    public static Node jjtCreate(EcmaScript p, int id) {
        return new ASTPostfixExpression(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}