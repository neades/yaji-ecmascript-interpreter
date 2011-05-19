//----------------------------------------------------------------------------
//
// Module:      FesiPop.java      
//
// Description: Static defines for all SimpleSelect classes
//
// Copyright:   (C) 1998 Jean-Marc Lugrin.  All rights reserved.
//              You may study, use, modify and distribute this example
//              for any purpose, provided that this copyright notice
//              appears in all copies.  This example is provided WITHOUT
//              WARRANTY either expressed or implied.
//----------------------------------------------------------------------------

// FESI example:
// Demonstrate how to create and use an extension.

// See explanations below for adaptation to your environment.
// To compile:  javac FesiPop.java (requires fesi.jar in the classpath)
// To run: start FESI from this directory (requires . and fesi.jar in the classpath)

import FESI.jslib.*;
import com.oroinc.net.pop3.*;
import java.io.*;

/** 
 * The FesiPop extensions uses the ORO Net Components to create
 * a POP3 interface.
 * <P>A POP3 object represents a connection to a server. It must have
 * a server, a username and a password attributes. The functions
 * are defined on a POP3prototype, which is the prototype of all
 * POP3 objects.<BR>
 * The POP3MSG objects represent the message headers. They have
 * an index, a from and a subject attribute. The array of 
 * messages is stored in the message attribute of the POP3
 * object.<P>
 * Only getting the list of message and getting 1 message text
 * is implemented, more capabilities may be implemented by
 * the user.
 * <P>The design of this extension is not optimal, it was choosen
 * to illustrate various relevant programming techniques, so the
 * same function was implemented in various ways along the program.
 * This is not a recommended practice!
 */
public class FesiPop implements JSExtension {

  private static String eol = System.getProperty("line.separator", "\n");
  
  private final static String statusNotConnected = "NOT CONNECTED";
  private final static String statusConnected = "CONNECTED";
  private final static String statusInError = "IN ERROR";
  
    /**
     * Create a simple interpreter with a few standard extensions,
     * which can be used to test this package.
     *
     * @param   args ignored
     */
    public static void main(String args[]) {
        
        System.out.println("FesiPop test interpreter");
        
        String[] extensions = new String[] {"FESI.Extensions.BasicIO",
                                            "FESI.Extensions.FileIO",
                                            "FESI.Extensions.JavaAccess",
                                            "FesiPop"};
                                            
        JSObject global = null;
        try {
            global = JSUtil.makeEvaluator(extensions);
        } catch (JSException e) {
            System.err.println("Cannot initialize FESI");
            System.err.println("Error " + e.getMessage());
            System.exit(1);
        }
        DataInputStream ins = new DataInputStream(System.in);
        String input = null;
        while (true) {
            System.out.print("> "); System.out.flush();
            try {
                input = ins.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println(e);
                System.exit(1);
            }
            if (input == null) break;
            if (input.equals("")) break;
            try {
                Object result = global.eval(input);
                if (result!=null) System.out.println(result.toString());
            } catch (JSException e) {
                System.out.println("** Error evaluating '" + input + "'");
                System.out.println(e.getMessage());
            }
        } // while
        System.out.println("FesiPop exiting");
    }
    

    /**
     * Initialize the FesiPop extension.
     * <P>Creates a POP3 type (of prototype POP3prototype)
     * which implements various POP3 functions. A default
     * error handler is created too.
     *
     * @param   go  The global object of this interpreter
     * @exception   JSException  Throwed if any error is detected
     */
    public void initializeExtension(JSGlobalObject go) throws JSException {
        // This illustrate a way to transfer a variable number of
        // arguments to a Java routine.
        final JSGlobalObject finalGo = go;
        try {
    
            // Create the prototype
            final JSObject pop3Prototype = go.makeJSObject();
            
            // Initialize the prototype with various functions and variables
            pop3Prototype.setMember("version", new Double(2.0));
            pop3Prototype.setMember("onError", go.eval("new Function('e','alert(e);return e.toString();');"));
    
            // Directly define a function as a subclas of JSFunction. The
            // function here simply call another function doing the implementation,
            // to keep the setup code clear. 
            pop3Prototype.setMember("connect", 
                new JSFunctionAdapter() {
                    public Object doCall (JSObject thisObject, Object [] args) {
                        return FesiPop.connect(thisObject);
                    };
                });
                
                                         
            // Define a function directly calling the target Java functions
            // (no bridge or JSFunction). Avoid this unlike there are few
            // parameters and no risk of confusion. 
            pop3Prototype.setMember("disconnect",
                go.eval("new Function('" +
                            "this.pop3client.logout();"+ // Will delete marked messages
                            "this.pop3client.disconnect();"+
                            "this.status=\""+statusNotConnected+"\"')"));

           // Setup a function to custom print the value of a pop3 connection
            pop3Prototype.setMember("toString", 
                new JSFunctionAdapter() {
                    public Object doCall (JSObject thisObject, Object [] args) throws JSException {
                        Object server = thisObject.getMember("server");
                        Object username = thisObject.getMember("username");
                        Object password = thisObject.getMember("password");
                        Object status = thisObject.getMember("status");
                        StringBuffer buf = new StringBuffer();
                        buf.append("[object POP3 server=");
                        buf.append(server);
                        buf.append(", username=");
                        buf.append(username);
                        buf.append(", password=");
                        buf.append(password.equals("")?"":"***");
                        buf.append("; ");
                        buf.append(status);
                        buf.append("]");
                        return buf.toString();
                    };
                });

            // Create the POP3 constructor - the code is inline with the
            // definition (OK if it is short)
            JSFunction POP3 = 
                new JSFunction() {
                    // Return a new POP3 connection object with optional values
                    // for server, username and password
                    public Object doNew (JSObject thisObject, Object [] args) throws JSException {
                        JSObject pop3  = finalGo.makeJSObject(pop3Prototype);
                        int n = args.length;
                        pop3.setMember("server", (n>0) ? args[0].toString() : "");
                        pop3.setMember("username", (n>1) ? args[1].toString() : "");
                        pop3.setMember("password", (n>2) ? args[2].toString() : "");
                        POP3Client pop3client = new POP3Client();
                        pop3client.setDefaultTimeout(60000);
                        pop3.setMember("pop3client",pop3client);
                        pop3.setMember("status", statusNotConnected); 
                        return pop3;
                    };
                    // Used as call is the same as new.
                    public Object doCall (JSObject thisObject, Object [] args) throws JSException {
                        return doNew(thisObject, args);
                    };
                };
            go.setMember("POP3", POP3);
            
            // POP3Message
            // Create the prototype
            JSObject pop3MSGPrototype = go.makeJSObject();

            // Function to get a specific message - bridge to a static function
            pop3MSGPrototype.setMember("getMessageText",
                go.eval("new Function('m','" +
                            "var text=Package.FesiPop.getMessageText(this);" +
                            "return text;')"));
 
             // Function to makr a message for deletion
             pop3Prototype.setMember("deleteMessage", 
                new JSFunctionAdapter() {
                    public Object doCall (JSObject thisObject, Object [] args) throws JSException {
                        JSObject connection = (JSObject) thisObject.getMember("connection");
                        POP3Client pop3client = (POP3Client) connection.getMember("pop3client");
                        String sid = thisObject.getMember("index").toString();
                        int id = Integer.parseInt(sid);
                        boolean status = false;
                        try {
                             status = pop3client.deleteMessage(id);
                        } catch (IOException e) {
                            // ignore
                        }
                        return new Boolean(status);
                    };
                });

            
           // Setup a function to custom print the value - the function
           // is directly defined as an EcmaScript function in this
           // case.
            pop3MSGPrototype.setMember("toString", 
                go.eval("new Function('" +
                            "return \"subject: \" + this.subject + " +
                            "\", from: \"+ this.from;');"));
           
            // We save the prototype in a convenient place, as we need it later
            // It should not be in a global variable, as multiple instances of
            // the extension could be active in one executable.
            go.setMember("pop3MSGPrototype", pop3MSGPrototype);
                            
                   
        } catch (JSException e) {
               // To help debug the above code               
               e.printStackTrace();
               throw e;
        }
    }
    
    /**
     * Create a POP3Message object from Java code
     */
    static JSObject makePOP3Message(JSGlobalObject go,
                                    JSObject connection,
                                    int id, 
                                    String from, 
                                    String subject) 
                           throws JSException  {
            JSObject pop3MSGPrototype = (JSObject) go.getMember("pop3MSGPrototype");
            JSObject pop3msg  = go.makeJSObject(pop3MSGPrototype);
            pop3msg.setMember("connection",  connection);
            pop3msg.setMember("index",  new Integer(id));
            pop3msg.setMember("from", from);
            pop3msg.setMember("subject",  subject);
            return pop3msg;
    }

    /**
     * Connect function, call the onError in case of problem.
     * Uses the properties of the connection object as parameter
     *
     * @param   connection  
     * @return     
     */
    public static String connect(JSObject connection) {
        try {
            String status = (String) connection.getMember("status"); 
            if (!status.equals(statusNotConnected)) {
                throw new JSException("Bad status: " + status);
            }
            String server = (String) connection.getMember("server");
            String username = (String) connection.getMember("username");
            String password = (String) connection.getMember("password");
            if (server.length()==0 || username.length()==0 || password.length()==0) {
                throw new JSException("server, username or password not specified");
            }
            POP3Client pop3client = (POP3Client) connection.getMember("pop3client");
            pop3client.connect(server);
            if (!pop3client.login(username,password)) {
                pop3client.disconnect();
                throw new JSException("username/password not accepted");
            }
            
            loadMessageList(connection, pop3client);
            
            connection.setMember("status", statusConnected); 
        } catch (Exception e) {
            // e.printStackTrace();
            String err = e.getClass().getName() + ": " + e.getMessage();
            try {
               return connection.eval("this.onError('"+err+"');").toString();
            } catch (JSException ignored) {
                // ignored.printStackTrace();
                return err;
            }
        }
        return ""; // == false
    }
    

    /**
     * Get the list of messages, keep them as an array of the connection element
     *
     * @param   connection  
     * @param   pop3client  
     */
    private static void loadMessageList(JSObject connection, 
                                        POP3Client pop3client)
             throws JSException, IOException {
        JSGlobalObject go = connection.getGlobalObject();
        JSObject array = go.makeJSArrayObject();
        POP3MessageInfo[] messages;
        POP3Client pop3 = (POP3Client) connection.getMember("pop3client");
        messages = pop3.listMessages();
        if(messages == null) {
            try {
                pop3.disconnect();
            } catch (Exception e) {
               // ignore
            }
            throw new JSException("Could not retrieve message list.");
        }
        
        // Load the header and id of all messages
        for(int message = 0; message < messages.length; message++) {
            int id = messages[message].number;
            Reader msgReader = pop3.retrieveMessageTop(id, 0);

            if(msgReader == null) {
               pop3.disconnect();
               throw new JSException("Could not retrieve message header.");
            }

            String line, lower, from, subject;
            BufferedReader reader = new BufferedReader(msgReader);
        
            from    = "";
            subject = "";
        
            while((line = reader.readLine()) != null) {
              lower = line.toLowerCase();
              if(lower.startsWith("from: "))
                 from = line.substring(6).trim();
              else if(lower.startsWith("subject: "))
                 subject = line.substring(9).trim();
            }
            JSObject pop3msg = makePOP3Message(go, connection, id, from, subject);
            array.setSlot(message, pop3msg);
            //System.out.println(Integer.toString(id) + " From: " + from +
            //           "  Subject: " + subject);
        } // for
        connection.setMember("messageList", array);
        return; 
    }

    /**
     * Get the text of a messages
     *
     * @param   message 
     * @return  "" in case of success, the error text in case of error 
     */
    public static String getMessageText(JSObject message) throws JSException {
        
        // Check if the text was read already
        try {
            String body = (String) message.getMember("body");
            if (body!= null) return body;
        } catch (JSException ignored) { }
        
        // No, must get it
        StringBuffer messageText = new StringBuffer("");
        JSObject connection = (JSObject) message.getMember("connection");
        String status = (String) connection.getMember("status");
        if (!status.equals(statusConnected)) {
            throw new JSException("Wrong connection status: " + status);
        }
       // try {
            POP3Client pop3client = (POP3Client) connection.getMember("pop3client");
                
            String sid = message.getMember("index").toString();
            int id = Integer.parseInt(sid);
            Reader msgReader = null;
            try {
                msgReader = pop3client.retrieveMessage(id);
                if(msgReader == null) {
                  pop3client.disconnect();
                  throw new JSException("Could not retrieve message.");
                }
            } catch (IOException e) {
                connection.setMember("status", statusInError);
                throw new JSException("IO error retrieving message.");
            }

            String line;
            BufferedReader reader = new BufferedReader(msgReader);
        
            try {
                while((line = reader.readLine()) != null) {
                    messageText.append(line);
                    messageText.append(eol);
                }
                reader.close();
            } catch (IOException e) {
                connection.setMember("status", statusInError);
                throw new JSException("IO error retrieving message.");
            }
       // } catch (Exception e) {
            // e.printStackTrace();
            // return e.getClass().getName() + ": " + e.getMessage();
       //     String err = e.getClass().getName() + ": " + e.getMessage();
       //     try {
       //        return connection.eval("this.onError('"+err+"');").toString();
       //     } catch (JSException ignored) {
       //         return err;
       //     }
       // }
        String text = messageText.toString();
        message.setMember("body", text);
        return text;
    }

}