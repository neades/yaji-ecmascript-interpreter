//----------------------------------------------------------------------------
//
// Module:      SimpleIntrp.java      
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
// FESI examples
// Demonstrate how to create and call an interpreter.

// To compile:  javac SimpleIntrp.java (requires fesi.jar in the classpath)
// To run: java SimpleIntrp (requires . and fesi.jar in the classpath)
// 
//  At each prompt ">" you can enter a command:
//    > 2+1
//    3
//    > exit()
//

import java.io.*;

import FESI.jslib.*;

/**
 * Main and only class of SimpleIntrp - a demonstration interpreter
 */
public class SimpleIntrp {
  
    // The evaluator
    static JSGlobalObject global = null;

    /**
     * Enter a read-eval-print main loop for FESI
     *
     * @param   args  ignored
     */
    public static void main(String args[]) {
        
        System.out.println("SimpleIntrp - demonstrate a simplistic interpreter of EcmaScript");
        
        // Build the interpreter with mininal extensions
        String[] extensions = new String[] {"FESI.Extensions.BasicIO",
                                            "FESI.Extensions.FileIO"};
        try {
            global = JSUtil.makeEvaluator(extensions);
        } catch (JSException e) {
            System.err.println("Cannot initialize FESI");
            System.err.println("Error " + e.getMessage());
            System.exit(1);
        }
        
        // Create the stream to interpret
        DataInputStream ins = new DataInputStream(System.in);
        String input = null;
        
        // Main read eval print loop
        while (true) {
            
            // Read a command
            System.out.print("> "); System.out.flush();
            try {
                input = ins.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println(e);
                System.exit(1);
            }
            
            // Exit on empty line or EOF
            if (input == null) break;
            if (input.equals("")) break;
            
            // Evaluate and print if any result
            try {
                Object result = global.eval(input); // Basic evaluation
                // Object result = global.evalAsFunction(input); // Basic evaluation
                if (result!=null) System.out.println(result.toString());
            } catch (JSException e) {
                System.out.println("** Error evaluating '" + input + "'");
                System.out.println(e.getMessage());
            }
        } // while
        
        System.out.println("SimpleIntrp exiting");
    }
 }