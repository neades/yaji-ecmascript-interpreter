/* Generated By:JJTree: Do not edit this line. ASTPropertyIdentifierReference.java */

package FESI.AST;

import FESI.Parser.EcmaScript;

public class ASTPropertyIdentifierReference extends SimpleNode {
    /**
	 * 
	 */
    private static final long serialVersionUID = -2384922162400406759L;

    public ASTPropertyIdentifierReference(int id) {
        super(id);
    }

    public ASTPropertyIdentifierReference(EcmaScript p, int id) {
        super(p, id);
    }

    public static Node jjtCreate(int id) {
        return new ASTPropertyIdentifierReference(id);
    }

    public static Node jjtCreate(EcmaScript p, int id) {
        return new ASTPropertyIdentifierReference(p, id);
    }

    /** Accept the visitor. **/
    @Override
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}