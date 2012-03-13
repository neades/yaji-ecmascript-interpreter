package FESI.Parser;


import java.io.StringReader;

import org.junit.Test;

import FESI.Data.EvaluatorTestCase;
import FESI.Exceptions.SyntaxError;
import FESI.Interpreter.PackagedException;

public class EcmascriptParserStrictModeTest extends EvaluatorTestCase {

    @Test(expected=SyntaxError.class)
    public void shouldParseProgramWithUseStrictFailsWithArguments() throws Exception {
        expectPackagedException("'use strict'; arguments = 0;");
    }

    @Test(expected=SyntaxError.class)
    public void cannotAssignToEvalInStrictMode() throws Exception {
        expectPackagedException("'use strict'; eval = 0;");
    }

    @Test(expected=SyntaxError.class)
    public void cannotPostIncrementEvalInStrictMode() throws Exception {
        expectPackagedException("'use strict'; eval ++;");
    }

    @Test(expected=SyntaxError.class)
    public void cannotDuplicatePropertyInObjectLiteral() throws Exception {
        expectPackagedException("'use strict'; var a = { x: 1, x: 2};");
    }

    @Test(expected=SyntaxError.class)
    public void cannotCallPropertyEval() throws Exception {
        expectPackagedException("'use strict'; var a = { eval: 2};");
    }

    @Test(expected=SyntaxError.class)
    public void cannotCallPropertyArguments() throws Exception {
        expectPackagedException("'use strict'; var a = { arguments: 2};");
    }

    @Test(expected=SyntaxError.class)
    public void cannotDefineTwoGetAccessors() throws Exception {
        expectPackagedException("'use strict'; var a = { get x() { return 1; }, get x() { return 2; } };");
    }

    @Test(expected=SyntaxError.class)
    public void cannotDefineTwoSetAccessors() throws Exception {
        expectPackagedException("'use strict'; var a = { set x(x) { x; }, set x(x) { x; } };");
    }

    @Test(expected=SyntaxError.class)
    public void cannotDefineDataAndGetAccessor() throws Exception {
        expectPackagedException("'use strict'; var a = { x:1, get x() { return 2; } };");
    }

    @Test(expected=SyntaxError.class)
    public void cannotDefineDataAndSetAccessor() throws Exception {
        expectPackagedException("'use strict'; var a = { x:1, set x(y) { y; } };");
    }

    @Test(expected=SyntaxError.class)
    public void cannotDefineSetAccessorAndData() throws Exception {
        expectPackagedException("'use strict'; var a = { set x(y) { y; }, get x() { return 1; }, x:0 };");
    }

    @Test(expected=SyntaxError.class)
    public void cannotPreIncrementEvalInStrictMode() throws Exception {
        expectPackagedException("'use strict'; ++eval;");
    }

    @Test(expected=SyntaxError.class)
    public void functionWithUseStrictFailsWithArguments() throws Exception {
        expectPackagedException("arguments = 0; function f() { 'use strict'; arguments = 0; }");
    }

    @Test
    public void shouldParseProgramWithFunctionUseStrict() throws Exception {
        EcmaScript ecmaScript = new EcmaScript(new StringReader("arguments = 0; function f() { 'use strict'; } arguments = 1;"));
        ecmaScript.Program();
    }

    @Test(expected=SyntaxError.class)
    public void functionDeclarationWithUseStrictFailsWithArguments() throws Exception {
        expectPackagedException("function f() { 'use strict'; eval = 0; }");
    }

    @Test(expected=SyntaxError.class)
    public void functionExpressionWithUseStrictFailsWithArguments() throws Exception {
        expectPackagedException("var f = function() { 'use strict'; eval = 0; }");
    }

    @Test(expected=SyntaxError.class)
    public void shouldNotAllowEvalInVariableDeclaration() throws Exception {
        expectPackagedException("'use strict'; var eval = 0;");
    }

    @Test(expected=SyntaxError.class)
    public void shouldNotAllowWithInStrictMode() throws Exception {
        expectPackagedException("'use strict'; with({ x:10 }) { y = x; }");
    }

    @Test
    public void shouldParseProgramWithoutUseStrict() throws Exception {
        EcmaScript ecmaScript = new EcmaScript(new StringReader("arguments = 0;"));
        ecmaScript.Program();
    }

    @Test(expected=SyntaxError.class)
    public void shouldntAllowOctalLiterals() throws Exception {
        expectPackagedException("'use strict'; var x=010;");
    }
    
    @Test(expected=SyntaxError.class)
    public void shouldntAllowOctalLiteralsInStrings() throws Exception {
        expectPackagedException("'use strict'; var x='\\377';");
    }
    private void expectPackagedException(String programText)
            throws ParseException, Exception {
        EcmaScript ecmaScript = new EcmaScript(new StringReader(programText));
        try {
            ecmaScript.Program();
        } catch ( PackagedException e ) {
            throw e.getPackage();
        }
    }

}
