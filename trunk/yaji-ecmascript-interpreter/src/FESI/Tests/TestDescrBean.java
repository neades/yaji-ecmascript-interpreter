// TestDescrBean.java
// FESI Copyright (c) Jean-Marc Lugrin, 1999
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package FESI.Tests;

/**
 * File used by the validation suite added to the FESI jar file for convenience
 * of validating the system
 */

public class TestDescrBean {

    private int intValue = 9; // Magic value, checked by test program

    private int[] intArray = new int[] { 1, 2, 3, 4, 5 }; // Magic value,
                                                          // checked by test
                                                          // program
    private int[] intIndexed = new int[] { 11, 12, 13, 14, 15 }; // Magic value,
                                                                 // checked by
                                                                 // test program

    public int getAValue() {
        return intValue;
    }

    public void setAValue(int v) {
        intValue = v;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public void setIntArray(int[] newIntArray) {
        intArray = newIntArray;
    }

    public void setIntArrayWithIndex(int index, int value) {
        intArray[index] = value;
    }

    public int getIntArrayWithIndex(int index) {
        return intArray[index];
    }

    public void setIntIndexed(int index, int value) {
        intIndexed[index] = value;
    }

    public int getIntIndexed(int index) {
        return intIndexed[index];
    }

    public String toString() {
        StringBuffer v = new StringBuffer("TestDescrBean: intValue=" + intValue
                + ", intArray=[");
        for (int i = 0; i < intArray.length; i++) {
            if (i > 0)
                v.append(",");
            v.append(Integer.toString(intArray[i]));
        }
        v.append("], intIndexed=[");
        for (int i = 0; i < intIndexed.length; i++) {
            if (i > 0)
                v.append(",");
            v.append(Integer.toString(intIndexed[i]));
        }
        v.append("]");
        return v.toString();
    }
}