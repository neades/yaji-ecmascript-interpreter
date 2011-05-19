//----------------------------------------------------------------------------
//
// Module:      SampleFunc.java      
//
// Description: Static defines for all SimpleSelect classes
//
// Copyright:   (C) 1998 Jean-Marc Lugrin.  All rights reserved.
//              You may study, use, modify and distribute this example
//              for any purpose, provided that this copyright notice
//              appears in all copies.  This example is provided WITHOUT
//              WARRANTY either expressed or implied.
//----------------------------------------------------------------------------

//
// FESI examples:
// Demonstrate how to create and call a script host, function
// objects and an error handler in EcmaScript, using the FESI
// java library.

// To compile:  javac SampleFunc.java (requires fesi.jar in the classpath)
// To run: java SampleFunc (requires . and fesi.jar in the classpath)
// You should see:
//	  SampleFunc - demonstrate calling EcmaScript
//    I am displaying ...
//     ... the page ...
//                   ... Hello.html
//    *ERROR* Page 'Wrong.html' not recognized
//    I am displaying ...
//     ... the page ...
//                   ... Bye.html


import FESI.jslib.*;

public class SampleFunc {
  
     static JSGlobalObject global = null;

     public static void main(String args[]) {
         System.out.println("SampleFunc - demonstrate calling EcmaScript");
         
         SampleFunc SampleFunc = new SampleFunc();
         try {
             // Interpret some user macro - assume we read it from some initialization file
             String userCommand = 
                     "onError = new Function('x', 'displayError(x)');\n" +
                     "displayPage('Hello.html')\n" +
                     "displayPage('Wrong.html')\n" + // Will cause an error
                     "displayPage('Bye.html')"; 
             global.eval(userCommand); 
             
         } catch (Exception e) {
             e.printStackTrace();
             System.err.println(e);
         }
     }
     
     
     // This is private, as it is not to be accessible by EcmaScript
     private SampleFunc() {
         try {
             // Create interpreter 
             global = JSUtil.makeEvaluator(); 
             // Setup a procedure displayPage and a procedure displayError, 
             // which call the correspdoning routines of this class
			JSFunction displayPageF = new JSFunctionAdapter() {
				public Object doCall(JSObject thisObject, Object args[]) throws JSException {
					if (args.length == 0) throw new JSException("displayPage: At least one argument needed");
					displayPage(args[0].toString());
					return null;
				}
			};

			 global.setMember("displayPage", displayPageF); 
             global.setMember("displayError", 
			    new JSFunctionAdapter() {
					public Object doCall(JSObject thisObject, Object args[]) throws JSException {
						if (args.length == 0) throw new JSException("displayError: At least one argument needed");
				        System.err.println("*ERROR* " + args[0]);
						return null;
					}
			    }); 
         } catch (Exception e) {
             e.printStackTrace();
             System.err.println(e);
         }
     }
     
     // Routine implementing code accessible from the script
     private void displayPage(String name) { 
     
        if (name.startsWith("Hello") || name.startsWith("Bye")) {
            
            // Simulate the display of a page if it is part of a recognized set
            
            System.out.println("I am displaying ..."); 
            System.out.println(" ... the page ...");
            System.out.println("               ... " + name);
            
        } else {
            
            // Demonstrate how an error handler in EcmaScript can be called
            
            try {
                global.call("onError", new Object[] {"Page '" + name + "' not recognized"});
            } catch (JSException e) {
                System.out.println("E: " + e);
                // No error handler present in user code - we generate an exception
                throw new IllegalArgumentException("Page '" + name  + "' not recognized");
            }
        } 
     }
     
 
 }