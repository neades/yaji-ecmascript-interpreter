//----------------------------------------------------------------------------
//
// Module:      SimpleIntrp2.java      
//
// Description: Static defines for all SimpleSelect classes
//
// Copyright:   (C) 1998 Jean-Marc Lugrin.  All rights reserved.
//              You may study, use, modify and distribute this example
//              for any purpose, provided that this copyright notice
//              appears in all copies.  This example is provided WITHOUT
//              WARRANTY either expressed or implied.
//
// Extended by Joh Johannsen to support entering multiple line 
// functions (until braces matches) or multi line strings (if the
// line is terminated by \n).
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
                                            "FESI.Extensions.FileIO",
                                            "FESI.Extensions.JavaAccess"};
        try {
            global = JSUtil.makeEvaluator(extensions);
        } catch (JSException e) {
            System.err.println("Cannot initialize FESI");
            System.err.println("Error " + e.getMessage());
            System.exit(1);
        }
        
        // Create the stream to interpret
        InputStreamReader isr = new InputStreamReader( System.in ); //
        BufferedReader ins = new BufferedReader( isr );
        String input = null;
        
        // Main read eval print loop
        StringBuffer sbin = new StringBuffer();
        int lbcount = 0;
        int rbcount = 0;
        String inputToEval = null;
        boolean extended = false;
        while (true) {
            // Read a command
            if ( extended ) 
                System.out.print("==> ");
            else if (lbcount==rbcount)
                System.out.print("> "); 
            else
                System.out.print("--> ");
            System.out.flush();
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
            int ilen = input.length();
            for ( int i = 0; i < ilen; i++ ) {
                char ichar = input.charAt( i );
                if ( ichar == '{' ) {
                    lbcount++;
                } else if ( ichar == '}' ) {
                    rbcount++;
                }
            }
            extended = ( input.lastIndexOf( "\\" ) == ( input.length() - 1 ));
            if ( extended ) {
                input = input.substring( 0, input.lastIndexOf( "\\" ));
            }
            sbin.append( input );
            if (( lbcount == rbcount ) && 
                  !(( input.indexOf( "function" ) == 0 ) && (lbcount == 0 )) && 
                  !extended ) {
                inputToEval = sbin.toString();
                sbin = new StringBuffer();
                lbcount = rbcount = 0;

                // Evaluate and print if any result
                try {
                    Object result = global.eval(inputToEval); // Basic evaluation
                    // Object result = global.evalAsFunction(input); // Basic evaluation
                    if (result!=null) System.out.println(result.toString());
                } catch (JSException e) {
                    System.out.println("** Error evaluating '" + input + "'");
                    System.out.println(e.getMessage());
                }
            } else if ( !extended ) {
                sbin.append( " " );
            }
            
        } // while
        
        System.out.println("SimpleIntrp exiting");
    }
 }
