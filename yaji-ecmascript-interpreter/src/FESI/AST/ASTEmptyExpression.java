/* Generated By:JJTree: Do not edit this line. ASTEmptyExpression.java */

package FESI.AST;

import FESI.Parser.EcmaScript;

public class ASTEmptyExpression extends SimpleNode {
    /**
	 * 
	 */
    private static final long serialVersionUID = -6784857639590742852L;

    public ASTEmptyExpression(int id) {
        super(id);
    }

    public ASTEmptyExpression(EcmaScript p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTEmptyExpression(id);
    }

    public static Node jjtCreate(EcmaScript p, int id) {
        return new ASTEmptyExpression(p, id);
    }

    /** Accept the visitor. **/
    @Override
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}