package org.yaji.util;

import java.util.HashMap;


public class UInt32BitSet {

    static class Bits64 {
        private static long [] mask = new long[64];
        static {
            long l = 1;
            for(int i=0; i<64; i++) {
                mask[i] = l;
                l <<= 1;
            }
        }
        long l;

        public void set(int i) {
            l |= mask[i];
        }

        public boolean get(int m) {
            return (l & mask[m]) != 0;
        }

        public void clear(int i) {
            l &= ~mask[i];
        }

        public void setAll() {
            l = -1;
        }

        public int lastSetBit(int startBit) {
            if (l == 0L) {
                return -1;
            }
            do {
                long tester = mask[startBit];
                if ((tester & l) != 0) {
                    return startBit;
                }
                startBit --;
            } while (startBit >= 0);
            return -1;
        }
        
    }
    static class HashMapBits64 extends HashMap<Integer,Bits64> {
        private static final long serialVersionUID = 5419571987339703999L;

        Bits64 getOrCreate(Integer startKey) {
            Bits64 bits = get(startKey);
            if (bits == null) {
                bits = new Bits64();
                put(startKey, bits);
            }
            return bits;
        }
        
    }
    private HashMapBits64 hashMapBits = new HashMapBits64(); 
    
    public void set(long start, long end) {
        if (end<start) {
            throw new IndexOutOfBoundsException("end must be after start");
        }
        int roundedStart = (int)(start/64L);
        int roundedEnd = (int)(end/64L);
        Integer startKey = Integer.valueOf((int)(start/64L));
        if (roundedStart == roundedEnd) {
            setBitsInWord(startKey, (int)(start%64), (int)(end%64));
        } else {
            setBitsInWord(startKey, (int)(start%64), 64);
            for(int i=roundedStart+1; i<roundedEnd; i++) {
                Integer key = Integer.valueOf(i);
                Bits64 bits = hashMapBits.getOrCreate(key);
                bits.setAll();
            }
            Integer endKey = Integer.valueOf((int)(end/64L));
            setBitsInWord(endKey,0,(int)(end%64));
        }
    }

    private void setBitsInWord(Integer startKey, int startBit, int endBit) {
        Bits64 bits = hashMapBits.getOrCreate(startKey);
        for (int i=startBit; i<endBit; i++) {
            bits.set(i);
        }
    }

    public void set(long index) {
        Integer key = Integer.valueOf((int)(index/64L));
        Bits64 bits = hashMapBits.getOrCreate(key);
        bits.set((int)(index%64));
    }
    
    public boolean get(long index) {
        Integer key = Integer.valueOf((int)(index/64L));
        Bits64 bits = hashMapBits.get(key);
        return bits != null && bits.get((int) (index%64));
    }

    public void clear(long index) {
        Integer key = Integer.valueOf((int)(index/64L));
        Bits64 bits = hashMapBits.get(key);
        if (bits != null) {
            bits.clear((int)(index%64));
        }
    }

    public long lastSetBit(long startPoint) {
        int start = (int)(startPoint/64L);
        Bits64 bits = hashMapBits.get(Integer.valueOf(start));
        int indexSet = (bits==null)?-1:bits.lastSetBit((int)(startPoint%64));
        while (indexSet == -1 && start >= 0) {
            start --;
            bits = hashMapBits.get(Integer.valueOf(start));
            indexSet = (bits==null)?-1:bits.lastSetBit(63);
        }
        if (indexSet == -1) {
            return -1L;
        }
        return start*64L + indexSet;
    }

}
