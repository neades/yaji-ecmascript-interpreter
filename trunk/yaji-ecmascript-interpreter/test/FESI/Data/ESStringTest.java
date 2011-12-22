package FESI.Data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ESStringTest {

    @Test
    public void toNumberHandlesInfinity() throws Exception {
        ESString string = new ESString("Infinity");
        assertTrue(Double.isInfinite(string.toESNumber().doubleValue()));
    }

    @Test
    public void toNumberHandlesHex() throws Exception {
        ESString string = new ESString("0x0002");
        assertEquals(2.0,string.toESNumber().doubleValue(),0.0);
    }

    @Test
    public void toNumberHandlesLeadingAndTrailingWhitespace() throws Exception {
        ESString string = new ESString(StringPrototypeTest.NON_ASCII_WHITE_SPACE+StringPrototypeTest.ASCII_WHITE_SPACE+"0x0002"+StringPrototypeTest.NON_ASCII_WHITE_SPACE+StringPrototypeTest.ASCII_WHITE_SPACE);
        assertEquals(2.0,string.toESNumber().doubleValue(),0.0);
    }

    @Test
    public void toNumberHandlesLeadingAndTrailingWhitespaceBlankIsZero() throws Exception {
        ESString string = new ESString(StringPrototypeTest.NON_ASCII_WHITE_SPACE+StringPrototypeTest.ASCII_WHITE_SPACE+StringPrototypeTest.NON_ASCII_WHITE_SPACE+StringPrototypeTest.ASCII_WHITE_SPACE);
        assertEquals(0.0,string.toESNumber().doubleValue(),0.0);
    }

    @Test
    public void toNumberConvertsToNegativeZero() throws Exception {
        ESString string = new ESString("-000000000000000000");
        double doubleValue = string.toESNumber().doubleValue();
        assertEquals(0.0,doubleValue,0.0);
        assertTrue(Double.valueOf(-0.0).equals(Double.valueOf(doubleValue)));
    }

}
