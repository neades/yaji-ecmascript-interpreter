/* Generated By:JJTree: Do not edit this line. ASTAllocationExpression.java */

package FESI.AST;

import FESI.Parser.EcmaScript;

public class ASTAllocationExpression extends SimpleNode {
    private static final long serialVersionUID = 7880238550615291445L;

    public ASTAllocationExpression(int id) {
        super(id);
    }

    public ASTAllocationExpression(EcmaScript p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTAllocationExpression(id);
    }

    public static Node jjtCreate(EcmaScript p, int id) {
        return new ASTAllocationExpression(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}