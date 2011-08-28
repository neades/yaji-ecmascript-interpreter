package org.yaji.debugger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.yaji.json.JsonUtil;

import FESI.Data.ESBoolean;
import FESI.Data.ESNull;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.ObjectObject;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.Version;

public class V8Debugger {

    private abstract class CommandHandler {
        public abstract void invoke(ESObject jsonValue, ESObject response) throws EcmaScriptException;
    }
    private Map<String,CommandHandler> commandMap = new HashMap<String,CommandHandler>() {
        private static final long serialVersionUID = 1L;
        {
            put("version",new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) throws EcmaScriptException {
                    response.putProperty("success", ESBoolean.valueOf(true));
                    ESObject body = ObjectObject.createObject(evaluator);
                    body.putProperty("V8Version", ESString.valueOf(Version.Level));
                    response.putProperty("body", body);
                    response.putProperty("running", ESBoolean.valueOf(debugger.isPaused()));
                }
            });
            put("continue", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) throws EcmaScriptException {
                    response.putProperty("success", ESBoolean.valueOf(true));
                    response.putProperty("running", ESBoolean.valueOf(true));
                }
            });
            put("evaluate", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) throws EcmaScriptException {
                    ESObject arguments = (ESObject) request.getProperty("arguments","arguments".hashCode());
                    String expression = arguments.getProperty("expression","expression".hashCode()).toString();
                    boolean noException = true;
                    ESValue result = ESNull.theNull;
                    try {
                        result = evaluator.evaluate(expression);
                        response.putProperty("body", new ESString(JsonUtil.stringify(evaluator,result,ESUndefined.theUndefined, ESUndefined.theUndefined)));
                    } catch (EcmaScriptException e) {
                        e.printStackTrace();
                        noException = false;
                    } catch (IOException e) {
                        e.printStackTrace();
                        noException = false;
                    }
                    
                    response.putProperty("running", ESBoolean.valueOf(!debugger.isPaused()));
                    response.putProperty("succcess", ESBoolean.valueOf(noException));
                }
            });
            put("lookup", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("backtrace", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("frame", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("scope", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("scopes", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("scripts", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("source", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("setbreakpoint", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("changebreakpoint", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("clearbreakpoint", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("setexceptionbreak", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("v8flags", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("profile", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("disconnect", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("gc", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
            put("listbreakpoints", new CommandHandler() {
                @Override
                public void invoke(ESObject request, ESObject response) {
                }
            });
        }
    };
    private final Debugger debugger;
    private final Evaluator evaluator;
    private int seq;

    public V8Debugger(Debugger debugger,Evaluator evaluator) {
        this.debugger = debugger;
        this.evaluator = evaluator;
    }

    public void handle(ESObject request, Result result) throws EcmaScriptException {
        String commandType = request.getProperty("command","command".hashCode()).toString();
        CommandHandler handler = commandMap.get(commandType);
        ESObject response = ObjectObject.createObject(evaluator);
        response.putProperty("seq", ESNumber.valueOf(seq++));
        response.putProperty("request_seq", request.getProperty("seq","seq".hashCode()));
        response.putProperty("type", ESString.valueOf("response"));
        response.putProperty("command", ESString.valueOf(commandType));
        if (handler != null) {
            handler.invoke(request, response);
            result.setCode(V8ResultCode.OK);
        } else {
            result.setCode(V8ResultCode.UNKNOWN_COMMAND);
        }
        result.setData(response);
    }

}
