// $codepro.audit.disable debuggingCode
package FESI.AST;

import java.io.PrintStream;

public class EcmaScriptDumpVisitor extends AbstractEcmaScriptVisitor {
    private int indent = 0;
    private PrintStream outStream;

    public EcmaScriptDumpVisitor() {
        outStream = System.out;
    }
    
    public EcmaScriptDumpVisitor(PrintStream out) {
        outStream = out;
    }
    
    private String indentString() {
        if (indent <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(indent);
        for (int i = 0; i < indent; ++i) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private Object dump(SimpleNode node, Object data) {
        getStream().println(indentString() + node);
        ++indent;
        data = node.childrenAccept(this, data);
        --indent;
        return data;
    }

    @Override
    public Object defaultAction(SimpleNode node, Object data) {
        return dump(node, data);
    }
    
    private PrintStream getStream() {
        return outStream;
    }
}
