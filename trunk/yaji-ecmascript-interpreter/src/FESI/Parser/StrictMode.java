package FESI.Parser;

import java.util.HashMap;
import java.util.HashSet;

import FESI.AST.ASTAssignmentExpression;
import FESI.AST.ASTCompositeReference;
import FESI.AST.ASTFunctionDeclaration;
import FESI.AST.ASTFunctionExpression;
import FESI.AST.ASTGetAccessor;
import FESI.AST.ASTIdentifier;
import FESI.AST.ASTLiteral;
import FESI.AST.ASTObjectLiteral;
import FESI.AST.ASTPostfixExpression;
import FESI.AST.ASTProgram;
import FESI.AST.ASTPropertyNameAndValue;
import FESI.AST.ASTSetAccessor;
import FESI.AST.ASTStatement;
import FESI.AST.ASTStatementList;
import FESI.AST.ASTUnaryExpression;
import FESI.AST.ASTVariableDeclaration;
import FESI.AST.ASTWithStatement;
import FESI.AST.AbstractEcmaScriptVisitor;
import FESI.AST.Node;
import FESI.AST.SimpleNode;
import FESI.Exceptions.ProgrammingError;
import FESI.Exceptions.SyntaxError;
import FESI.Interpreter.PackagedException;

public class StrictMode extends AbstractEcmaScriptVisitor {
    
    public static final String EXCEPTION_PREFIX = "Strict mode restriction: ";

    private static class StrictModeState {

        private boolean strict;

        public StrictModeState(ASTProgram program, boolean strictMode) {
            strict = strictMode;
        }

        public StrictModeState(StrictModeState outerState) {
            strict = outerState.strict;
        }
        
        public void setStrictMode() {
            strict = true;
        }

        public boolean isStrictMode() {
            return strict;
        }
        
    }
    
    public static SimpleNode validate(ASTProgram program, boolean strictMode) {
        StrictMode strictModeVisitor = new StrictMode();
        StrictModeState state = new StrictModeState(program, strictMode);
        lookAheadForDirectives(program,state);
        strictModeVisitor.visit(program, state);
        program.setStrictMode(state.isStrictMode());
        return program;
    }

    private static void lookAheadForDirectives(Node statementList,
            StrictModeState state) {
        if (hasStrictModeDirective(statementList)) {
            state.setStrictMode();
        }
    }
    
    public static boolean hasStrictModeDirective(Node statementList) {
        int child = 0;
        boolean canContinue = true;
        while ( canContinue && child < statementList.jjtGetNumChildren()) {
            canContinue = false;
            Node node = statementList.jjtGetChild(child);
            if (node instanceof ASTStatement) {
                ASTStatement statement = (ASTStatement) node;
                if (statement.jjtGetNumChildren() == 1) {
                    Node possibleLiteral = node.jjtGetChild(0);
                    if (possibleLiteral instanceof ASTLiteral) {
                        ASTLiteral literal = (ASTLiteral) possibleLiteral;
                        String string = literal.getValue().toString();
                        if ("use strict".equals(string)) {
                            return true;
                        }
                        canContinue = true;
                    }
                }
            }
            child ++;
        }
        return false;
    }

    @Override
    public Object visit(ASTFunctionDeclaration node, Object data) {
        StrictModeState strictMode = new StrictModeState((StrictModeState)data);
        ASTStatementList statementList = (ASTStatementList) node.jjtGetChild(2);
        lookAheadForDirectives(statementList, strictMode);
        node.setStrictMode(strictMode.isStrictMode());
        return super.visit(node, strictMode);
    }
    
    @Override
    public Object visit(ASTFunctionExpression node, Object data) {
        StrictModeState strictMode = new StrictModeState((StrictModeState)data);
        ASTStatementList statementList = (ASTStatementList) node.jjtGetChild(node.jjtGetNumChildren()-1);
        lookAheadForDirectives(statementList, strictMode);
        node.setStrictMode(strictMode.isStrictMode());
        return super.visit(node, strictMode);
    }
    
    private static class GetOrSet {
        private final String propertyName;
        private boolean isGet;
        private boolean isSet;

        public GetOrSet(String propertyName, boolean isGet, boolean isSet) {
            this.propertyName = propertyName;
            this.isGet = isGet;
            this.isSet = isSet;
            
        }

        public String getName() {
            return propertyName;
        }

        public boolean matches(GetOrSet other) {
            return (isSet && other.isSet) || (isGet && other.isGet);
        }

        public GetOrSet merge(GetOrSet existing) {
            isSet |= existing.isSet;
            isGet |= existing.isGet;
            return this;
        }
    }
    
    @Override
    public Object visit(ASTObjectLiteral node, Object data) {
        if (isStrictMode(data)) {
            HashMap<String, GetOrSet> propertyNames = new HashMap<String, GetOrSet>();
            int numChildren = node.jjtGetNumChildren();
            for (int i=0; i<numChildren; i++) {
                Node child = node.jjtGetChild(i);
                GetOrSet property = getPropertyName((ASTPropertyNameAndValue)child);
                throwSyntaxIfinvalidIdentifierInStrictMode(node,property.getName());
                GetOrSet existing = propertyNames.get(property.getName());
                if (existing != null) {
                    if ( existing.matches(property)) {
                        throw createException("Property name "+property.getName()+" is repeated in object literal",node);
                    }
                    propertyNames.put(property.getName(), property.merge(existing));
                } else {
                    propertyNames.put(property.getName(), property);
                }
            }
        }
        return super.visit(node, data);
    }
    
    private GetOrSet getPropertyName(ASTPropertyNameAndValue node) {
        Node child = node.jjtGetChild(0);
        if (child instanceof ASTIdentifier) {
            return new GetOrSet(((ASTIdentifier)child).getName(),true,true);
        }
        boolean isSet = false;
        boolean isGet = false;
        if (child instanceof ASTGetAccessor) {
            isGet = true;
        } else if (child instanceof ASTSetAccessor) {
            isSet = true;
        }
        child = node.jjtGetChild(1);
        if (child instanceof ASTIdentifier) {
            return new GetOrSet(((ASTIdentifier)child).getName(), isGet, isSet);
        }
        throw new ProgrammingError("Invalidly formatted property name");
    }

    private boolean isStrictMode(Object data) {
        StrictModeState state = (StrictModeState)data;
        return state.isStrictMode();
    }

    @Override
    public Object visit(ASTUnaryExpression node, Object data) {
        checkEvalAndArguments(node, data, 1);
        return super.visit(node, data);
    }
    
    @Override
    public Object visit(ASTAssignmentExpression node, Object data) {
        checkEvalAndArguments(node, data, 0);
        return super.visit(node, data);
    }

    @Override
    public Object visit(ASTPostfixExpression node, Object data) {
        checkEvalAndArguments(node, data, 0);
        return super.visit(node, data);
    }
    
    @Override
    public Object visit(ASTVariableDeclaration node, Object data) {
        checkEvalAndArguments(node, data, 0);
        return super.visit(node, data);
    }
    
    @Override
    public Object visit(ASTWithStatement node, Object data) {
        if (isStrictMode(data)) {
            throw createException("'with' statement not permitted.", node);
        }
        return super.visit(node, data);
    }
    
    private static final HashSet<String> strictModeReserved = new HashSet<String>() {
        private static final long serialVersionUID = -6226874034788426181L;
        {
            add("implements");
            add("let");
            add("private");
            add("public");
            add("interface");
            add("package");
            add("protected");
            add("static");
            add("yield");
        }
    };
     
    @Override
    public Object visit(ASTIdentifier node, Object data) {
        if (isStrictMode(data) && isStrictModeReserved(node.getName())) {
            throw createException(node.getName() + " is reserved in strict mode", node);
        }
        return super.visit(node, data);
    }

    private boolean isStrictModeReserved(String name) {
        return strictModeReserved.contains(name);
    }

    private void checkEvalAndArguments(SimpleNode node, Object data, int childIndex) {
        if (isStrictMode(data)) {
            Node lrefNode = node.jjtGetChild(childIndex);
            String referencedName = "";
            if (lrefNode instanceof ASTCompositeReference) {
                lrefNode = lrefNode.jjtGetChild(lrefNode.jjtGetNumChildren()-1);
            }
            if (lrefNode instanceof ASTIdentifier) {
                referencedName = ((ASTIdentifier)lrefNode).getName();
            }
            throwSyntaxIfinvalidIdentifierInStrictMode(node, referencedName);
        }
    }

    private void throwSyntaxIfinvalidIdentifierInStrictMode(SimpleNode node, String identifier) {
        if (identifier.equals("arguments") || identifier.equals("eval")) {
            throw createException("Cannot assign to '"+identifier+"'", node);
        }
    }

    private PackagedException createException(String reason, SimpleNode node) {
        return new PackagedException(new SyntaxError(EXCEPTION_PREFIX + reason), node);
    }
    
    @Override
    public Object defaultAction(SimpleNode node, Object data) {
        node.childrenAccept(this, data);
        return data;
    }
}
