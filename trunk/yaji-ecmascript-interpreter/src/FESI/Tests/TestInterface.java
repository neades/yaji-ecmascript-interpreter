// TestInterface.java
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

public interface TestInterface {

    public int getData();

    // A static class should be accessible
    public static class InsideValue {
        static public InsideValue create() {
            return new InsideValue();
        }

        public int increment(int value) {
            return value + 1;
        }
    }
}