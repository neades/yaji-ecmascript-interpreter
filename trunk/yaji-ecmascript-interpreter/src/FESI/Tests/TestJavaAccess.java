// TestJavaAccess.java
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

import java.util.Enumeration;
import java.util.Vector;

import FESI.jslib.JSException;
import FESI.jslib.JSFunction;
import FESI.jslib.JSFunctionAdapter;
import FESI.jslib.JSGlobalObject;
import FESI.jslib.JSObject;

/**
 * File used by the validation suite added to the FESI jar file for convenience
 * of validating the system
 */

public class TestJavaAccess implements Runnable {

    // To check parameter matching in constructors
    public String createdBy = "none";

    // For regression testing of an error
    private static int counter = 0;
    private int corbaValue = 0;

    static class ImageClass {
        private int n;

        public ImageClass() {
            super();
            n = ++counter;
        }

        public String toString() {
            return "This is image " + n;
        }
    }

    // Object is Runnable just to be use as a parameter in some routine
    public void run() {
        // so not really runnable
    }

    // Test static fields
    public static int staticIntField = 1; // Magic value, checked by test
                                          // program

    public static int getStaticIntField() {
        return staticIntField;
    }

    // Test instance fields
    public int intField = 100; // Magic value, checked by test program
    private int privateIntField = 101; // Magic value, checked by test program

    // Instance tests

    // Constructor for standard testes
    public TestJavaAccess() {
        super();
        images.addElement(new ImageClass());
        images.addElement(new ImageClass());
        images.addElement(new ImageClass());
    }

    // Consturctor to check distance algorithm and widening
    public TestJavaAccess(byte b) {
        super();
        createdBy = "byte";
    }

    // Consturctor to check distance algorithm and widening
    public TestJavaAccess(short b) {
        super();
        createdBy = "short";
    }

    // Consturctor to check distance algorithm and widening
    public TestJavaAccess(int b) {
        super();
        createdBy = "int";
    }

    // Consturctor to check distance algorithm and widening
    public TestJavaAccess(long b) {
        super();
        createdBy = "long";
    }

    // Consturctor to check distance algorithm and widening
    public TestJavaAccess(float b) {
        super();
        createdBy = "float";
    }

    // Consturctor to check distance algorithm and widening
    public TestJavaAccess(double b) {
        super();
        createdBy = "double";
    }

    // Public method to access private fields
    public void setPrivateIntField(int i) {
        privateIntField = i;
    }

    public int getPrivateIntField() {
        return privateIntField;
    }

    public int getIntField() {
        return intField;
    }

    // Test widening (basic types)
    public String checkWidening(byte b) {
        return "byte widening";
    }

    public String checkWidening(short s) {
        return "short widening";
    }

    public String checkWidening(int i) {
        return "int widening";
    }

    public String checkWidening(long l) {
        return "long widening";
    }

    public String checkWidening(float f) {
        return "float widening";
    }

    public String checkWidening(double d) {
        return "double widening";
    }

    // Check arrays are returned types
    public byte[] getArray() {
        byte[] is = new byte[] { 1, 2, 3 };
        return is;
    }

    // Check arrays as parameter type
    public double meanArray(byte[] a) {
        double m = 0;
        for (int i = 0; i < a.length; i++) {
            m += a[i];
        }
        return m / a.length;
    }

    // For test via EcmaScript keywords that are not Java keyword
    // (requires '[]' syntax)
    public String in = "inField";

    public String var(String s) {
        return s + "+var";
    }

    // For get/set using Corba standard
    public int corba() {
        return corbaValue;
    }

    public void corba(int v) {
        corbaValue = v;
    }

    // For automatic test
    public Object testJS(JSObject o) {
        boolean ok = false;
        try {
            // Check that we can access member values
            Object a = o.getMember("a"); // should be 1
            Object b = o.getMember("b"); // should be 2
            ok = ((Number) a).intValue() + 1 == ((Number) b).intValue();

            // Check that we can evaluate some text returning a value
            Object res = o.eval("o.a + 3;"); // should be 4
            o.setMember("r", res); // The script will check that is does
                                   // evaluate correctly

            // Check that we can evaluate some text not returning a value
            // This crashed in version 0.6
            Object noRes = o.eval("function dummy() { writeln(1234) };");
            if (noRes != null)
                return Boolean.valueOf(false);

            // This function takes the "separator" attribute of the object,
            // and use it to surround the string value of the first parameter
            JSFunction jsf = new JSFunctionAdapter() {
                private static final long serialVersionUID = -4070423414417723904L;

                public Object doCall(JSObject thisObject, Object args[])
                        throws JSException {
                    String sep = thisObject.getMember("separator").toString();
                    if (sep == null)
                        throw new JSException(
                                "jsf: Missing attribute separator");
                    if (args.length < 2)
                        throw new JSException(
                                "jsf: At least one argument needed");
                    if (!(args[0] instanceof JSObject))
                        throw new JSException(
                                "Expected a JSobejct as first parameter");
                    JSObject a0 = (JSObject) args[0];
                    if (args[1] instanceof JSObject)
                        throw new JSException(
                                "Expected NON JSobejct as second parameter");
                    String s = a0.getMember("str").toString();
                    return sep + s + sep + args[1].toString() + sep;
                }
            };
            o.setMember("jsf", jsf);

            // Test that we can wrap the object to access its fieldd via "this"
            JSGlobalObject go = o.getGlobalObject();
            TestJavaAccess jac = new TestJavaAccess();
            JSObject js = go.makeObjectWrapper(jac);
            js.eval("this.intField=300;");
            if (jac.intField != 300) {
                throw new Exception("this.intField=300; failed");
            }

            // test Function on an object
            String[] names1 = { "p1", "p2" };
            Object[] values1 = { Integer.valueOf(3), "intField" };
            js.evalAsFunction("this[p2]=p1;", names1, values1);
            if (jac.intField != 3) {
                throw new Exception("evalAsFunction 1 failed, intField = "
                        + jac.intField);
            }

            // test more wrap conditions
            Object bound = js
                    .evalAsFunction("var myo = new Object(); myo.test=321; return myo");
            JSObject jBound = null;
            if (bound instanceof JSObject) {
                jBound = (JSObject) bound;
            } else {
                throw new Exception(
                        "Script 'var myo = new Object(); myo.test=321; return myo' did not return an object");
            }
            String[] names2 = { "p1", "p2" };
            Object[] values2 = { Integer.valueOf(4), jBound };
            js.evalAsFunction("this.intField=p2.test+p1;", names2, values2);
            if (jac.intField != 325) {
                throw new Exception("evalAsFunction 2 failed, intField = "
                        + jac.intField);
            }

            // Check this access in function
            Object forthis = js
                    .eval("forthis = new Object(); forthis.test = 12; forthis");
            JSObject ftw = go.makeObjectWrapper(forthis);
            Object ftr = ftw.eval("test");
            Number fti = (Number) ftr;
            if (fti.intValue() != 12) {
                throw new Exception("this at eval did not work " + fti);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.toString());
            return Boolean.valueOf(false);
        }
        return Boolean.valueOf(ok);
    }

    // To check that access of an interface routine is possible
    public boolean testInterface(Runnable r) {
        r.run();
        return true;
    }

    public LocalForInterface getSubInterfaceData() {
        return new LocalForInterface();
    }

    // Check that a new object can be returned and used,
    // the object is a non public class
    public SomeLocalStuff makeStuff() {
        return new SomeLocalStuff();
    }

    public SomeLocalSubclass makeSub() {
        return new SomeLocalSubclass();
    }

    // Check that an object can be created and added as an element
    // (the calling program will call its functions).
    public void makeStuffElement(JSObject o) {
        SomeLocalStuff sls = new SomeLocalStuff();
        try {
            o.setMember("sls", sls);
        } catch (JSException e) {
            // ignore
        }
    }

    // For the test of non public sub-class routines
    public String whichOne() {
        return "main";
    }

    // To get a bean as an object
    public void makeObj(JSObject o) throws JSException {
        TestJavaBean tjb = new TestJavaBean();
        o.setMember("asObj", tjb);
        tjb.visible = 1;
    }

    // To get a bean as a bean
    public void makeBean(JSObject o) throws JSException {
        TestJavaBean tjb = new TestJavaBean();
        tjb.visible = -1;
        o.setMember("asBean", o.getGlobalObject().makeBeanWrapper(tjb));
    }

    // To get a bean as a bean
    public void makeWrapped(JSObject o) throws JSException {
        TestJavaBean tjb = new TestJavaBean();
        tjb.visible = 0;
        o.setMember("asWrapped", o.getGlobalObject().makeObjectWrapper(tjb));
    }

    public void testUnwrap(JSObject o) throws JSException {
        JSGlobalObject go = o.getGlobalObject();
        {
            Object asObj = o.getMember("asObj");
            if (asObj == null)
                throw new NullPointerException("asObj");
            // System.out.println("CLSS " + asObj.getClass().getName());
            TestJavaBean asObj1 = (TestJavaBean) asObj;
            if (asObj1.visible != 1)
                throw new RuntimeException("Unexpected obj returned");
        }
        {
            Object asObj = o.getMember("asBean");
            if (asObj == null)
                throw new NullPointerException("asBean");
            // System.out.println("CLSS " + asObj.getClass().getName());
            TestJavaBean asObj1 = (TestJavaBean) asObj;
            if (asObj1.visible != -1)
                throw new RuntimeException("Unexpected bean returned");
        }
        {
            Object asObj = o.getMember("asWrapped");
            if (asObj == null)
                throw new NullPointerException("asWrapped");
            // System.out.println("CLSS " + asObj.getClass().getName());
            TestJavaBean asObj1 = (TestJavaBean) asObj;
            if (asObj1.visible != 0)
                throw new RuntimeException("Unexpected obj returned");
        }
        {
            TestJavaBean tjb = new TestJavaBean();
            Object utjb = go.getWrappedObject(go.makeObjectWrapper(tjb));
            if (utjb != tjb)
                throw new RuntimeException("wrap/unrwap error - object");
            utjb = go.getWrappedBean(go.makeBeanWrapper(tjb));
            if (utjb != tjb)
                throw new RuntimeException("wrap/unrwap error - bean");
        }

    }

    public Enumeration<ImageClass> getImages() {
        return images.elements();
    }

    private Vector<ImageClass> images = new Vector<ImageClass>();

    // For interactive test
    public Object testJSObject(JSObject o) {
        System.out.println("** TestJavaAccess: testJSObject - o = "
                + o.toString());
        try {
            Object a = o.getMember("a");
            Object b = o.getMember("b");
            System.out.println(">o.a is " + a.toString());
            System.out.println(">o.b is " + b.toString());
            Object res = o.eval("o.a + 3;");
            System.out.println(">eval o.a + 3 is " + res.toString());
            o.eval("writeln ('inside o: a is ' + a)");
            o.eval("writeln ('inside o: this.a is ' + this.a)");
            o.setMember("b", "beta");
            o.setMember("c", res);
            // o.setMember("c", "TEST");
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error: " + e.toString());
        }
        return o;
    }
}

// A non public subclass of a public class
class SomeLocalSubclass extends TestJavaAccess {
    public String whichOne() {
        return "sub";
    }
}

// Non public class, only routine inherited from a public class
// (in this case Object) can be called.

class SomeLocalStuff {
    public String toString() {
        return "local";
    }

    // will create an error, as there is no method named getName in
    // a public superclass of SomeLocalStuff
    public String getName() {
        return "error";
    }
}

// Non public class implementing an interface and a subinterface
class LocalForInterface implements TestSubInterface {
    public int getData() {
        return 42;
    }

    public int getSubData() {
        return 84;
    }

    public int getSubData(int value) {
        return value + 42;
    }
}
