package org.yaji.debugger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.GatheringByteChannel;

import org.yaji.json.JsonState;

import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.ObjectObject;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

class Result {
    private ResultCode code;
    private ESValue     data;
    private ESObject jsonObject;
    private static final JsonState INDENT = new JsonState(ESUndefined.theUndefined,ESUndefined.theUndefined);
    public Result(String commandType, Evaluator evaluator) throws EcmaScriptException {
        jsonObject = ObjectObject.createObject(evaluator);
        jsonObject.putProperty("command",ESString.valueOf(commandType));
    }

    public void writeTo(Command command, GatheringByteChannel debugConnection) throws IOException, EcmaScriptException {
        jsonObject.putProperty("result", ESNumber.valueOf(code.getCode()));
        if (data != null) {
            jsonObject.putProperty("data",data);
        }
        
        StringBuilder builder = new StringBuilder();
        StringBuilder content = new StringBuilder();
        jsonObject.toJson(content, INDENT, "");
        command.appendHeaders(builder,content.length());
        System.out.println("\n--> Sending to Client:\n"+builder.toString()+content.toString()+"\n");
        debugConnection.write(new ByteBuffer[] {ByteBuffer.wrap(builder.toString().getBytes("UTF-8")),
                ByteBuffer.wrap(content.toString().getBytes("UTF-8"))});
    }

    public void setData(ESValue data) {
        this.data = data;
    }

    public void setCode(ResultCode code) {
        this.code = code;
    }

}