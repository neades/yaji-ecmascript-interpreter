package org.yaji.debugger;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;

import org.junit.Test;

import FESI.Interpreter.Evaluator;

public class ResultTest {

    private static class TestByteChannel implements GatheringByteChannel {
        private byte[] output;

        public int write(ByteBuffer arg0) throws IOException {
            return 0;
        }

        public void close() throws IOException {
            // not used
        }

        public boolean isOpen() {
            return false;
        }

        public long write(ByteBuffer[] byteBuffers) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (ByteBuffer byteBuffer : byteBuffers) {
                baos.write(byteBuffer.array());
            }
            output = baos.toByteArray();
            return baos.size();
        }

        public long write(ByteBuffer[] arg0, int arg1, int arg2)
                throws IOException {
            return 0;
        }
    }
    
    @Test
    public void testWriteTo() throws Exception {
        String message = "Tool:DevToolsService\r\n" + 
                "Content-Length:21\r\n" + 
                "\r\n" +
                "{\"command\":\"version\"}"; 
        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes("UTF-8"));
        
        Command command = new Command();
        command.construct(buffer);
        
        Result result = new Result("version", new Evaluator());
        TestByteChannel outputChannel = new TestByteChannel();
        
        result.setCode(DevToolsResultCode.OK);
        result.writeTo(command, outputChannel);
        
        assertEquals("Content-Length:32\r\n" + 
        		"Tool:DevToolsService\r\n" + 
        		"\r\n" + 
        		"{\"command\":\"version\",\"result\":0}",new String(outputChannel.output,"UTF-8"));
    }

}
