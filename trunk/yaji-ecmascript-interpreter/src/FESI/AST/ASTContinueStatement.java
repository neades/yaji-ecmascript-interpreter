/* Generated By:JJTree: Do not edit this line. ASTContinueStatement.java */

package FESI.AST;

import FESI.Parser.EcmaScript;

public class ASTContinueStatement extends SimpleNode {
    /**
	 * 
	 */
    private static final long serialVersionUID = 8573772426599525318L;

    public ASTContinueStatement(int id) {
        super(id);
    }

    public ASTContinueStatement(EcmaScript p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTContinueStatement(id);
    }

    public static Node jjtCreate(EcmaScript p, int id) {
        return new ASTContinueStatement(p, id);
    }

    /** Accept the visitor. **/
    @Override
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}