// TestDescrBeanBeanInfo.java
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

import java.beans.BeanDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;

/**
 * File used by the validation suite added to the FESI jar file for convenience
 * of validating the system
 */

public class TestDescrBeanBeanInfo extends SimpleBeanInfo {

    public BeanDescriptor getBeanDescriptor() {
        return new BeanDescriptor(TestDescrBean.class);
    }

    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor intValue = new PropertyDescriptor("intValue",
                    TestDescrBean.class, "getAValue", "setAValue");

            IndexedPropertyDescriptor intArray = new IndexedPropertyDescriptor(
                    "intArray", TestDescrBean.class, "getIntArray",
                    "setIntArray", "getIntArrayWithIndex",
                    "setIntArrayWithIndex");
            intArray.setBound(false);
            intArray.setConstrained(false);

            IndexedPropertyDescriptor intIndexed = new IndexedPropertyDescriptor(
                    "intIndexed", TestDescrBean.class, null, null,
                    "getIntIndexed", "setIntIndexed");
            intArray.setBound(false);
            intArray.setConstrained(false);

            PropertyDescriptor rv[] = { intValue, intArray, intIndexed };
            return rv;

        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

}