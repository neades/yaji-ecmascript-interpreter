/* Generated By:JJTree: Do not edit this line. ASTForVarInStatement.java */

package FESI.AST;

import FESI.Parser.EcmaScript;

public class ASTForVarInStatement extends SimpleNode {
    /**
	 * 
	 */
    private static final long serialVersionUID = 3632969295835108386L;

    public ASTForVarInStatement(int id) {
        super(id);
    }

    public ASTForVarInStatement(EcmaScript p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTForVarInStatement(id);
    }

    public static Node jjtCreate(EcmaScript p, int id) {
        return new ASTForVarInStatement(p, id);
    }

    /** Accept the visitor. **/
    @Override
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}