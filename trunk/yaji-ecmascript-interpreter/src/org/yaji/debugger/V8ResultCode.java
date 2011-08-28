package org.yaji.debugger;

enum V8ResultCode implements ResultCode {
    OK (0), // the operation completed successfully (it is a tool-level result code, not a command-level one. For example, the result may be 0 even though the underlying debugger_command request failed. In this case OK means that a valid response has been received from the V8 debugger.)
    ILLEGAL_TAB_STATE (1), // the tab specified in the "Destination" header is in an inappropriate state (i.e. it is attached for an "attach" command or not attached for a "detach" command.)
    UNKNOWN_TAB (2), // the tab specified in the "Destination" header does not exist (it may have been reported in the "list_tabs" response but closed since then.)
    DEBUGGER_ERROR (3), // a generic error occurred while performing the specified operation.
    UNKNOWN_COMMAND (4); // the specified command is not found in the Available commands list.             
    private final int code;

    private V8ResultCode(int code) {
        this.code = code;
    }
    
    public int getCode() {
        return code;
    }
}