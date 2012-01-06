package FESI.Data;

import static org.junit.Assert.*;

import org.junit.Test;

public class ESNumberTest {

    @Test
    public void negativeZeroNotSameValueAsZero() throws Exception {
        ESNumber positiveZero = ESNumber.valueOf(0.0);
        ESNumber negativeZero = ESNumber.valueOf(-0.0);
        assertFalse(positiveZero.sameValue(negativeZero));
    }

    
}
