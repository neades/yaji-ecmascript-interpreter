package org.yaji.debugger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import FESI.Data.ArrayObject;
import FESI.Data.ArrayPrototype;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESValue;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class Debugger {

    private V8Debugger v8debugger;
    private final int port;

    public Debugger(int port) {
        this.port = port;
    }

    public void setEvaluator(Evaluator evaluator) {
        this.v8debugger = new V8Debugger(this,evaluator);
        new Thread(new DebugListener(this, port, evaluator),"YAJI Debugger").start();
    }

    private Object latch = new Object();
    private List<File> scripts = new ArrayList<File>();
    
    public void check(int lineNumber) {
        synchronized (latch) {
            try {
                latch.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static class DebugListener implements Runnable {

        private static final String CHROME_DEV_TOOLS_HANDSHAKE = "ChromeDevToolsHandshake\r\n";
        private ServerSocketChannel serverSocketChannel;
        private final int port;
        private final Debugger debugger;
        private final Evaluator evaluator;

        public DebugListener(Debugger debugger, int port, Evaluator evaluator) {
            this.debugger = debugger;
            this.port = port;
            this.evaluator = evaluator;
        }

        public void run() {
            try {
                serverSocketChannel = ServerSocketChannel.open();
                ServerSocket socket = serverSocketChannel.socket();
                socket.bind(new InetSocketAddress(port));
                do {
                    SocketChannel debugConnection = serverSocketChannel.accept();
                    handleDebugSession(debugConnection);
                } while(true);
            } catch (IOException e) {
                System.err.println("Debugger exiting");
                e.printStackTrace();
            } catch (EcmaScriptException e) {
                System.err.println("Debugger exiting");
                e.printStackTrace();
            }
        }

        private void handleDebugSession(SocketChannel debugConnection) throws EcmaScriptException {
            ByteBuffer buffer = ByteBuffer.allocate(2048);
            try {
                boolean connected = false;
                Command command = new Command();
                do {
                    buffer.clear();
                    int read = debugConnection.read(buffer);
                    if (!connected) {
                        connected = checkHandshake(debugConnection,
                                buffer, read);
                    } else {
                        buffer.flip();
                        while (buffer.hasRemaining()) {
                            if (command.construct(buffer)) {
                                Result result = dispatch(command);
                                if (result != null) {
                                    result.writeTo(command,debugConnection);
                                }
                            }
                        }
                    }
                    
                } while (true);
            } catch (IOException e) {
                System.err.println("Closing debug session");
                e.printStackTrace();
            }
        }

        private boolean checkHandshake(SocketChannel debugConnection,
                ByteBuffer buffer, int read)
                throws UnsupportedEncodingException, IOException {
            boolean nowConnected = false;
            String s = new String(buffer.array(),0,read,"UTF-8");
            if (CHROME_DEV_TOOLS_HANDSHAKE.equals(s)) {
                debugConnection.write(ByteBuffer.wrap(CHROME_DEV_TOOLS_HANDSHAKE.getBytes("UTF-8")));
                nowConnected = true;
            }
            System.out.println("\n-->Received by Debugger:\n"+s);
            return nowConnected;
        }

        private interface CommandHandler {
            public boolean apply(Debugger debugger, ESObject json, Result result, Evaluator evaluator) throws EcmaScriptException;
        }
        
        private static abstract class CommandSet extends HashMap<String,CommandHandler> {
            private static final long serialVersionUID = 1L;
            private final ResultCode defaultCode;
            public CommandSet(ResultCode defaultCode) {
                this.defaultCode = defaultCode;
            }
            public ResultCode getDefaultCode() {
                return defaultCode;
            }
        }
        
        private static Map<String,CommandSet> commandSets = new HashMap<String,CommandSet>();
        static {
            commandSets.put("DevToolsService", new CommandSet(DevToolsResultCode.UNKNOWN_COMMAND) {
                private static final long serialVersionUID = 1L;
                {
                    put("version",new CommandHandler() {
                        public boolean apply(Debugger debugger, ESObject commandContent, Result result, Evaluator evaluator) {
                            result.setData(ESString.valueOf("0.1"));
                            result.setCode(DevToolsResultCode.OK);
                            return true;
                        }
                    });
                    put("list_tabs",new CommandHandler() {
                        public boolean apply(Debugger debugger, ESObject commandContent, Result result, Evaluator evaluator) throws EcmaScriptException {
                            ArrayPrototype tabList = ArrayObject.createArray(evaluator);
                            ArrayPrototype defaultTab = ArrayObject.createArray(evaluator);
                            defaultTab.add(ESNumber.valueOf(0));
                            defaultTab.add(ESString.valueOf("Default"));
                            tabList.add(defaultTab);
                            result.setData(tabList);
                            result.setCode(DevToolsResultCode.OK);
                            return true;
                        }
                    });
                }
            });
            commandSets.put("V8Debugger", new CommandSet(V8ResultCode.UNKNOWN_COMMAND) {
                private static final long serialVersionUID = 1L;
                {
                    put("attach",new CommandHandler() {
                        public boolean apply(Debugger debugger, ESObject commandContent, Result result, Evaluator evaluator) {
                            result.setCode(V8ResultCode.OK);
                            return true;
                        }
                    });
                    put("debugger_command",new CommandHandler() {
                        public boolean apply(Debugger debugger, ESObject commandContent, Result result, Evaluator evaluator) throws EcmaScriptException {
                            ESValue jsonValue = commandContent.getProperty("data","data".hashCode());
                            if (jsonValue instanceof ESObject) {
                                debugger.getV8Debugger().handle((ESObject)jsonValue, result);
                            }
                            result.setCode(V8ResultCode.OK);
                            return true;
                        }
                    });
                    put("evaluate_javascript", new CommandHandler() {
                        public boolean apply(Debugger debugger, ESObject commandContent, Result result, Evaluator evaluator) {
                            return false;
                        }
                    });
                }
            });
        }
        
        private Result dispatch(Command command) throws EcmaScriptException {
            ESObject json = (ESObject) command.getContentAsJson(evaluator);
            String commandType = json.getProperty("command","command".hashCode()).toString();
            Result result = new Result(commandType, evaluator);
            
            String toolName = command.getHeader("Tool");
            boolean reply = true;
            CommandSet commandSet = commandSets.get(toolName);
            if (commandSet != null) {
                CommandHandler commandHandler = commandSet.get(commandType);
                if (commandHandler != null) {
                    reply = commandHandler.apply(debugger, json, result, evaluator);
                } else {
                    result.setCode(commandSet.getDefaultCode());
                }
            }
            return reply?result:null;
        }
        
    }

    protected V8Debugger getV8Debugger() {
        return v8debugger;
    }

    public boolean isPaused() {
        return false;
    }

    public void addScript(File file) {
        scripts.add(file);
    }

    public List<File> getScripts() {
        return scripts;
    }
}
