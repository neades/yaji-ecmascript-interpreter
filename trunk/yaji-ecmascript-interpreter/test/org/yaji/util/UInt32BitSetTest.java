package org.yaji.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class UInt32BitSetTest {

    private UInt32BitSet bitSet;


    @Before
    public void setUp() throws Exception {
        bitSet = new UInt32BitSet();
    }

    @Test
    public void initiallyClear() {
        assertFalse(bitSet.get(0L));
    }

    
    @Test
    public void setAndRetrieveBit() {
        bitSet.set(0L);
        assertTrue(bitSet.get(0L));
    }

    @Test
    public void setAndRetrieveBit63() {
        bitSet.set(63L);
        assertFalse(bitSet.get(62L));
        assertTrue(bitSet.get(63L));
    }

    @Test
    public void setAndRetrieveHighBit() {
        bitSet.set(0x80000000L);
        assertTrue(bitSet.get(0x80000000L));
    }

    @Test
    public void clear() {
        bitSet.set(123);
        bitSet.clear(123);
        assertFalse(bitSet.get(123));
    }
    
    @Test
    public void setRange() {
        bitSet.set(0,10);
        for(int i=0; i<10; i++) {
            assertTrue(bitSet.get(i));
        }
        assertFalse(bitSet.get(10L));
    }

    @Test
    public void setRangeSpanning2Words() {
        bitSet.set(60,70);
        assertFalse(bitSet.get(59L));
        for(int i=0; i<10; i++) {
            assertTrue(bitSet.get(60+i));
        }
        assertFalse(bitSet.get(70L));
    }

    @Test
    public void setRangeSpanning4Words() {
        bitSet.set(64,257);
        assertFalse(bitSet.get(63L));
        for(int i=0; i<193; i++) {
            assertTrue(bitSet.get(64+i));
        }
        assertFalse(bitSet.get(257L));
    }

    @Test
    public void findLastSetBit() {
        bitSet.set(100L);
        assertEquals(100L, bitSet.lastSetBit(200L));
    }

    @Test
    public void findLastSetBitForEmptySet() {
        assertEquals(-1, bitSet.lastSetBit(200L));
    }
}
