package org.yaji.debugger;

import FESI.AST.ASTStatement;
import FESI.Interpreter.EcmaScriptEvaluateVisitor;
import FESI.Interpreter.Evaluator;

public class DebugEcmaScriptEvaluateVisitor extends EcmaScriptEvaluateVisitor {

    private final Debugger debugger;

    public DebugEcmaScriptEvaluateVisitor(Evaluator evaluator, Debugger debugger) {
        super(evaluator);
        this.debugger = debugger;
    }

    @Override
    public Object visit(ASTStatement node, Object data) {
        debugger.check(node.getLineNumber());
        return super.visit(node, data);
    }
}
