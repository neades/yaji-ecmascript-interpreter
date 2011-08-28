package org.yaji.debugger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.nio.ByteBuffer;

import org.junit.Test;

import FESI.Data.ESObject;
import FESI.Interpreter.Evaluator;

public class CommandTest {

    @Test
    public void shouldParseCompleteMessage() throws Exception {
        String message = "Tool:DevToolsService\r\n" + 
                "Content-Length:21\r\n" + 
                "\r\n" + 
                "{\"command\":\"version\"}";
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
        
        Command command = new Command();
        assertTrue(command.construct(buffer));
        assertEquals("DevToolsService",command.getHeader("Tool"));
        assertEquals("{\"command\":\"version\"}",command.getContentAsString());
    }

    @Test
    public void shouldParseTwoMessages() throws Exception {
        String message = "Tool:DevToolsService\r\n" + 
                "Content-Length:21\r\n" + 
                "\r\n" + 
                "{\"command\":\"version\"}"+
                "Tool:V8Debugger\r\n" + 
                "Content-Length:23\r\n" + 
                "\r\n" + 
                "{\"command\":\"list_tabs\"}";
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
        
        Command command = new Command();
        assertTrue(command.construct(buffer));
        assertEquals("DevToolsService",command.getHeader("Tool"));
        assertEquals("{\"command\":\"version\"}",command.getContentAsString());
        assertTrue(command.construct(buffer));
        assertEquals("V8Debugger",command.getHeader("Tool"));
        assertEquals("{\"command\":\"list_tabs\"}",command.getContentAsString());
    }

    @Test
    public void shouldParseSplitMessage() throws Exception {
        String message = "Tool:DevToolsService\r\n" + 
                "Content-Length:21\r\n" + 
                "\r\n"; 
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
        
        Command command = new Command();
        assertFalse(command.construct(buffer));
        assertTrue(command.construct(ByteBuffer.wrap("{\"command\":\"version\"}".getBytes("UTF-8"))));
        assertEquals("DevToolsService",command.getHeader("Tool"));
        assertEquals("{\"command\":\"version\"}",command.getContentAsString());
    }

    @Test
    public void shouldParseSplitMessageOnCR() throws Exception {
        String message = "Tool:DevToolsService\r\n" + 
                "Content-Length:21\r\n" + 
                "\r"; 
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
        
        Command command = new Command();
        assertFalse(command.construct(buffer));
        assertTrue(command.construct(ByteBuffer.wrap("\n{\"command\":\"version\"}".getBytes("UTF-8"))));
        assertEquals("DevToolsService",command.getHeader("Tool"));
        assertEquals("{\"command\":\"version\"}",command.getContentAsString());
    }

    @Test
    public void shouldIgnoreCRInHeader() throws Exception {
        String message = "Tool:Dev\rTools\rService\r\n" + 
                "Content-Length:21\r\n" + 
                "\r\n" +
                "{\"command\":\"version\"}"; 
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
        
        Command command = new Command();
        assertTrue(command.construct(buffer));
        assertEquals("Dev\rTools\rService",command.getHeader("Tool"));
    }
    
    @Test
    public void shouldAllowDecodingOfContentAsJson() throws Exception {
        String message = "Tool:DevToolsService\r\n" + 
                "Content-Length:21\r\n" + 
                "\r\n" +
                "{\"command\":\"version\"}"; 
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
        
        Command command = new Command();
        assertTrue(command.construct(buffer));
        
        Object jsonObject = command.getContentAsJson(new Evaluator());
        if (jsonObject instanceof ESObject) {
            ESObject json = (ESObject) jsonObject;
            assertEquals("version",json.getProperty("command","command".hashCode()).toString());
        } else {
            fail("Should return jsonObject");
        }
    }
    
    @Test
    public void shouldWriteOutHeaders() throws Exception {
        String message = "Tool:DevToolsService\r\n" + 
                "Content-Length:21\r\n" + 
                "\r\n" + 
                "{\"command\":\"version\"}";
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
        
        Command command = new Command();
        assertTrue(command.construct(buffer));

        StringBuilder builder = new StringBuilder();
        command.appendHeaders(builder,34);
        
        assertEquals("Content-Length:34\r\n"
                + "Tool:DevToolsService\r\n"
                + "\r\n", builder.toString());
    }
}
