// TestJavaBean.java
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

public class TestJavaBean {
    // To test access via setter/getter
    private int hidden = 12; // Magic value, checked by test program

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int x) {
        hidden = x;
    }

    // To test direct access
    public int visible = 3; // Magic value, checked by test program

    // To test access of a public field with setter/getter when used
    // as a bean or as a field. Note the minus in getBoth()
    // to test if access was
    // via setter/getter or directly.
    public int both = 5; // Magic value, checked by test program

    public void setBoth(int x) {
        both = -x;
    }

    public int getBoth() {
        return -both;
    }

    public String toString() {
        return "TestJavaBean: visible=" + visible + ", hidden=" + hidden
                + ", both=" + both;
    }
}