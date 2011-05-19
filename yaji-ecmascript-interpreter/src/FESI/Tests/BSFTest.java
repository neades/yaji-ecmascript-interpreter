// FESI Copyright (c) Jean-Marc Lugrin, 2003
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

import java.util.Vector;

import org.apache.bsf.*;

import FESI.jslib.JSException;
import FESI.jslib.JSGlobalObject;
import FESI.jslib.JSObject;

/**
 * @author jmlugrin
 * 
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class BSFTest {

    public static void main(String args[]) {
        System.out.println("Testing BSF interface");
        try {

            BSFManager mgr = new BSFManager();

            String[] extensions = { "es" };
            BSFManager.registerScriptingEngine("ecmascript",
                    "FESI.Bsf.BsfEngine", extensions);

            // Check simple direct call
            Object result = mgr.eval("ecmascript", "testString", 0, 0, "2+32");
            checkResult(result, new Byte((byte) 34));

            // Check call with return
            result = mgr.apply("ecmascript", "testString", 0, 0, "return 4+6",
                    null, null);
            checkResult(result, new Byte((byte) 10));

            // Check call as a function with arguments
            Vector<String> argNames = new Vector<String>();
            argNames.add("arg_1");
            argNames.add("arg_2");
            Vector<Object> argValues = new Vector<Object>();
            argValues.add(new Integer(12));
            argValues.add(new Integer(14));
            result = mgr.apply("ecmascript", "testString", 0, 0,
                    "return arg_1 + arg_2", argNames, argValues);
            checkResult(result, new Byte((byte) 26));

            // Register a bean
            BsfTestBean testBean = new BsfTestBean();
            mgr.registerBean("TestBsf", testBean);

            // Access registered bean
            testBean.value = 50000;
            result = mgr.eval("ecmascript", "testString", 0, 0,
                    "x = bsf.lookupBean (\"TestBsf\");  x.value");
            checkResult(result, new Integer(50000));

            // Using a javascript object
            result = mgr.eval("ecmascript", "testString", 0, 0,
                    "o = new Object();  o.field=12.3; o");
            // System.out.println("O = " + result +
            // ", class = " + result.getClass().getName());
            JSObject jso = (JSObject) result;

            // We get the global object via the retrieved javascript object
            JSGlobalObject global = jso.getGlobalObject();

            // Set a value via the script and retrieve it via the global object
            result = mgr.eval("ecmascript", "testString", 0, 0,
                    "viaScript = 12.3;");
            checkResult(result, new Double(12.3));
            result = global.getMember("viaScript");
            // System.out.println("result = " + result +
            // ", class = " + result.getClass().getName());
            checkResult(result, new Double(12.3));

            // The engine was auto-loaded, access it explicitely
            // ot test getGlobalObject
            BSFEngine fesiEngine = mgr.loadScriptingEngine("ecmascript");
            // System.out.println("ENGINE: " + fesiEngine);
            FESI.Bsf.BsfEngine fe = (FESI.Bsf.BsfEngine) fesiEngine;
            JSGlobalObject g = fe.getJSGlobalObject();
            // Check that the global object is the same as the original
            // one (there is a single interpreter)
            Object r = g.getMember("viaScript");
            // System.out.println("r="+r);
            checkResult(r, new Double(12.3));

        } catch (BSFException ex) {
            System.out.println("Error testing BSF");
            ex.printStackTrace();
            System.exit(1);
        } catch (JSException ex) {
            System.out.println("Error testing BSF");
            ex.printStackTrace();
            System.exit(1);
        }

        System.out.println("BSFTest completed successfully");

    }

    private static void checkResult(Object result, Object expected) {
        if (!expected.equals(result)) {
            throw new RuntimeException("BSF test failed, expected " + expected
                    + " of class " + result.getClass().getName() + ", got "
                    + result + " of class " + result.getClass().getName());
        }
    }

    public static class BsfTestBean {
        public int value;
    }
}
