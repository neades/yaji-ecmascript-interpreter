package org.yaji.debugger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import FESI.Data.ESObject;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class V8DebuggerTest {

    private V8Debugger v8debugger;
    private Evaluator evaluator;
    private Result result;
    private Debugger debugger;
    private File tmpFile;

    private static class TestChannel implements GatheringByteChannel {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        public int write(ByteBuffer buffer) throws IOException {
            int count = 0;
            while (buffer.hasRemaining()) {
                baos.write(buffer.get());
                count ++;
            }
            return count;
        }

        public void close() throws IOException {
            // not needed
        }

        public boolean isOpen() {
            return true;
        }

        public long write(ByteBuffer[] arg0) throws IOException {
            long count = 0;
            for (ByteBuffer byteBuffer : arg0) {
                count += write(byteBuffer);
            }
            return count;
        }

        public long write(ByteBuffer[] arg0, int arg1, int arg2)
                throws IOException {
            return 0;
        }
        
    }
    
    @Before
    public void setUp() throws Exception {
        evaluator = new Evaluator();
        debugger = new Debugger(0);
        v8debugger = new V8Debugger(debugger, evaluator);
        result = new Result("debugger_command",evaluator);
    }

    @After
    public void tearDown() throws Exception {
        if (tmpFile != null) {
            if (!tmpFile.delete()) {
                tmpFile.deleteOnExit();
            }
        }
    }
    
/*
      Response:

      { "seq"         : <number>,
        "type"        : "response",
        "request_seq" : <number>,
        "type"        : "request",
        "body"        : { "V8Version":   <string, version of V8>
                        }
        "running"     : <is the VM running after sending this response>
        "success"     : true
      }
      */
    @Test
    public void testHandleVersion() throws Exception {
        
        String requestText = "Tool:V8Debugger\r\n" + 
                "Destination:2\r\n" + 
                "Content-Length:85\r\n" + 
                "\r\n" + 
                "{\"command\":\"debugger_command\",\"data\":{\"seq\":65,\"type\":\"request\",\"command\":\"version\"}}\n";
        String string = processV8Request(requestText);
        assertEquals("Content-Length:173\r\n" + 
                "Destination:2\r\n" + 
                "Tool:V8Debugger\r\n" + 
                "\r\n" + 
                "{\"command\":\"debugger_command\",\"data\":{\"body\":{\"V8Version\":\"1.1.8\"},\"command\":\"version\",\"request_seq\":65,\"running\":false,\"seq\":0,\"success\":true,\"type\":\"response\"},\"result\":0}", string);
    }
    
    @Test
    public void handleScriptsRequest() throws Exception {
        tmpFile = File.createTempFile("V8DebuggerTest", ".js");
        PrintWriter pw = new PrintWriter(tmpFile, "UTF-8");
        pw.append("The text of the script\nFun Fun Fun");
        pw.close();
        
        debugger.addScript(tmpFile);
        String requestText = "Tool:V8Debugger\r\n" + 
        		"Destination:0\r\n" + 
        		"Content-Length:129\r\n" + 
        		"\r\n" + 
        		"{\"command\":\"debugger_command\",\"data\":{\"seq\":6,\"type\":\"request\",\"command\":\"scripts\",\"arguments\":{\"includeSource\":true,\"types\":4}}}";
        String string = processV8Request(requestText);
        Pattern pattern = Pattern.compile("Content-Length:[0-9]*\r\n" + 
                "Destination:0\r\n" + 
                "Tool:V8Debugger\r\n" + 
                "\r\n" + 
                 "\\{\"command\":\"debugger_command\",\"data\":\\{"
                +"\"body\":\\[\\{\"id\":1,\"lineCount\":2,\"name\":\"V8DebuggerTest[0-9]*\\.js\","
                +"\"source\":\"The text of the script\\\\r\\\\nFun Fun Fun\\\\r\\\\n\"\\}\\],"
                +"\"command\":\"scripts\",\"request_seq\":6,\"running\":true,\"seq\":0,"
                +"\"success\":true,\"type\":\"response\""
                +"\\},\"result\":0\\}"
//                +".*"
                ,Pattern.MULTILINE);
        assertTrue(string,pattern.matcher(string).matches());
    }

    private String processV8Request(String requestText)
            throws UnsupportedEncodingException, EcmaScriptException,
            IOException {
        Command command = createCommand(requestText);
        
        v8debugger.handle(getV8Command(command), result);
        
        String string = writeResultToString(command);
        return string;
    }

    private String writeResultToString(Command command) throws IOException,
            EcmaScriptException {
        TestChannel testChannel = new TestChannel();
        result.writeTo(command, testChannel);
        
        return testChannel.baos.toString();
    }

    private ESObject getV8Command(Command command) throws EcmaScriptException {
        ESObject devToolsCommand = (ESObject)command.getContentAsJson(evaluator);
        return (ESObject)devToolsCommand.getProperty("data","data".hashCode());
    }

    private Command createCommand(String requestText)
            throws UnsupportedEncodingException {
        Command command = new Command();
        command.construct(ByteBuffer.wrap(requestText.getBytes("UTF-8")));
        return command;
    }

}
