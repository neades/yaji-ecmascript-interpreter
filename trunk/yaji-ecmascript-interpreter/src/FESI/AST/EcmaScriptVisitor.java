/* Generated By:JavaCC: Do not edit this line. EcmaScriptVisitor.java Version 5.0 */
package FESI.AST;


public interface EcmaScriptVisitor
{
  public Object visit(SimpleNode node, Object data);
  public Object visit(ASTLiteral node, Object data);
  public Object visit(ASTIdentifier node, Object data);
  public Object visit(ASTThisReference node, Object data);
  public Object visit(ASTSuperReference node, Object data);
  public Object visit(ASTCompositeReference node, Object data);
  public Object visit(ASTFunctionExpression node, Object data);
  public Object visit(ASTFormalParameterList node, Object data);
  public Object visit(ASTFunctionCallParameters node, Object data);
  public Object visit(ASTPropertyValueReference node, Object data);
  public Object visit(ASTPropertyIdentifierReference node, Object data);
  public Object visit(ASTAllocationExpression node, Object data);
  public Object visit(ASTArrayLiteral node, Object data);
  public Object visit(ASTElision node, Object data);
  public Object visit(ASTObjectLiteral node, Object data);
  public Object visit(ASTPropertyNameAndValue node, Object data);
  public Object visit(ASTGetAccessor node, Object data);
  public Object visit(ASTSetAccessor node, Object data);
  public Object visit(ASTOperator node, Object data);
  public Object visit(ASTPostfixExpression node, Object data);
  public Object visit(ASTUnaryExpression node, Object data);
  public Object visit(ASTBinaryExpressionSequence node, Object data);
  public Object visit(ASTAndExpressionSequence node, Object data);
  public Object visit(ASTOrExpressionSequence node, Object data);
  public Object visit(ASTConditionalExpression node, Object data);
  public Object visit(ASTAssignmentExpression node, Object data);
  public Object visit(ASTExpressionList node, Object data);
  public Object visit(ASTStatement node, Object data);
  public Object visit(ASTStatementList node, Object data);
  public Object visit(ASTVariableDeclaration node, Object data);
  public Object visit(ASTIfStatement node, Object data);
  public Object visit(ASTWhileStatement node, Object data);
  public Object visit(ASTForStatement node, Object data);
  public Object visit(ASTEmptyExpression node, Object data);
  public Object visit(ASTForVarStatement node, Object data);
  public Object visit(ASTForInStatement node, Object data);
  public Object visit(ASTForVarInStatement node, Object data);
  public Object visit(ASTContinueStatement node, Object data);
  public Object visit(ASTBreakStatement node, Object data);
  public Object visit(ASTReturnStatement node, Object data);
  public Object visit(ASTWithStatement node, Object data);
  public Object visit(ASTFunctionDeclaration node, Object data);
  public Object visit(ASTProgram node, Object data);
  public Object visit(ASTTryStatement node, Object data);
  public Object visit(ASTCatch node, Object data);
  public Object visit(ASTFinally node, Object data);
  public Object visit(ASTThrowStatement node, Object data);
  public Object visit(ASTSwitchStatement node, Object data);
  public Object visit(ASTCaseClause node, Object data);
  public Object visit(ASTDefaultClause node, Object data);
}
/* JavaCC - OriginalChecksum=812fdecff22152963bf5b8db5ac260b9 (do not edit this line) */
