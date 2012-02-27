package org.yaji.regex;

import FESI.Exceptions.EcmaScriptException;

public interface RegexImplementation {

    Pattern compile(String string, int flags) throws EcmaScriptException;
    
}