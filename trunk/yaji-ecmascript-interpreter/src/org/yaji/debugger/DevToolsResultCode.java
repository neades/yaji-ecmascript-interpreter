package org.yaji.debugger;

public enum DevToolsResultCode implements ResultCode {
    OK(0),
    UNKNOWN_COMMAND(1);
    
    private final int code;

    private DevToolsResultCode(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}