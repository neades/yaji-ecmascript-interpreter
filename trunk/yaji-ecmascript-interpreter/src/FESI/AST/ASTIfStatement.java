/* Generated By:JJTree: Do not edit this line. ASTIfStatement.java */

package FESI.AST;

import FESI.Parser.EcmaScript;

public class ASTIfStatement extends SimpleNode {
  /**
	 * 
	 */
	private static final long serialVersionUID = 6936757012529149605L;

public ASTIfStatement(int id) {
    super(id);
  }

  public ASTIfStatement(EcmaScript p, int id) {
    super(p, id);
  }

  public static Node jjtCreate(int id) {
      return new ASTIfStatement(id);
  }

  public static Node jjtCreate(EcmaScript p, int id) {
      return new ASTIfStatement(p, id);
  }

  /** Accept the visitor. **/
  public Object jjtAccept(EcmaScriptVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}