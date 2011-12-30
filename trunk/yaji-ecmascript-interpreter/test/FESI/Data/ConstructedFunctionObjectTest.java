package FESI.Data;

import org.junit.Before;
import org.junit.Test;

import FESI.Exceptions.SyntaxError;

public class ConstructedFunctionObjectTest extends EvaluatorTestCase{

    private ESValue functionObject;
    
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        functionObject = globalObject.getProperty("Function");
    }
    
    @Test(expected=SyntaxError.class)
    public void argumentsNotAllowedAsAFunctionParameter() throws Exception {
        functionObject.doConstruct(new ESValue[] { s("arguments"), s("'use strict';") } );
    }

    @Test(expected=SyntaxError.class)
    public void evalNotAllowedAsAFunctionParameter() throws Exception {
        functionObject.doConstruct(new ESValue[] { s("eval"), s("'use strict';") } );
    }

    @Test(expected=SyntaxError.class)
    public void duplicateArgNameNotAllowedAsAFunctionParameter() throws Exception {
        functionObject.doConstruct(new ESValue[] { s("a,a"), s("'use strict';") } );
    }

    private ESString s(String value) {
        return new ESString(value);
    }

}
