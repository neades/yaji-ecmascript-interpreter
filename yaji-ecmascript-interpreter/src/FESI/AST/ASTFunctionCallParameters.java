/* Generated By:JJTree: Do not edit this line. ASTFunctionCallParameters.java */

package FESI.AST;

import FESI.Parser.EcmaScript;

public class ASTFunctionCallParameters extends SimpleNode {
    /**
	 * 
	 */
    private static final long serialVersionUID = 8748577082578062012L;

    public ASTFunctionCallParameters(int id) {
        super(id);
    }

    public ASTFunctionCallParameters(EcmaScript p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTFunctionCallParameters(id);
    }

    public static Node jjtCreate(EcmaScript p, int id) {
        return new ASTFunctionCallParameters(p, id);
    }

    /** Accept the visitor. **/
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}