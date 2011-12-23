package FESI.Data;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class BooleanPrototypeTest extends EvaluatorTestCase {

    private ESObject booleanObject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        booleanObject = (ESObject) globalObject.getProperty("Boolean");
    }

    /**
     * Since Boolean is an object, instance toBoolean will return true
     * @throws Exception
     */
    @Test
    public void booleanToBooleanIsTrue() throws Exception {
        ESObject booleanInstance = booleanObject.doConstruct(new ESValue[] { ESBoolean.FALSE });
        assertTrue(booleanInstance.toESBoolean().booleanValue());
    }

}
