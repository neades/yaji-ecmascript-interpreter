/* Generated By:JJTree: Do not edit this line. ASTCaseClause.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package FESI.AST;

import FESI.Parser.*;

public
class ASTCaseClause extends SimpleNode {
    private static final long serialVersionUID = 1L;


    public ASTCaseClause(int id) {
        super(id);
    }

    public ASTCaseClause(EcmaScript p, int id) {
        super(p, id);
    }


    /** Accept the visitor. **/
    public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
/* JavaCC - OriginalChecksum=d640d52b73e2ebe2cc910b5493f4ffb7 (do not edit this line) */