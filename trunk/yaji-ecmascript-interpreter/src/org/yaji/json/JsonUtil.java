package org.yaji.json;

import java.io.IOException;

import FESI.Data.ESString;
import FESI.Data.ESValue;
import FESI.Data.ObjectObject;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public abstract class JsonUtil {

    public static String stringify(Evaluator evaluator, ESValue value, ESValue replacerFunction, ESValue indent) throws IOException, EcmaScriptException {
        StringBuilder sb = new StringBuilder();
        JsonState jsonState = new JsonState(replacerFunction,indent);
        value = jsonState.callReplacerFunction(ObjectObject.createObject(evaluator), ESString.valueOf(""), value );
        if (value.canJson()) {
            value.toJson(sb, jsonState, "");
            return sb.toString();
        }
        return null;
    }
    /*
     *         char = unescaped /
                escape (
                    %x22 /          ; "    quotation mark  U+0022
                    %x5C /          ; \    reverse solidus U+005C
                    %x2F /          ; /    solidus         U+002F
                    %x62 /          ; b    backspace       U+0008
                    %x66 /          ; f    form feed       U+000C
                    %x6E /          ; n    line feed       U+000A
                    %x72 /          ; r    carriage return U+000D
                    %x74 /          ; t    tab             U+0009
                    %x75 4HEXDIG )  ; uXXXX                U+XXXX

         escape = %x5C              ; \

         quotation-mark = %x22      ; "

         unescaped = %x20-21 / %x23-5B / %x5D-10FFFF
     */
    private static String [] hexCode = {
            "\\u0000", "\\u0001", "\\u0002", "\\u0003", "\\u0004", "\\u0005", "\\u0006", "\\u0007",      
            "\\u0008", "\\u0009", "\\u000A", "\\u000B", "\\u000C", "\\u000D", "\\u000E", "\\u000F",      
            "\\u0010", "\\u0011", "\\u0012", "\\u0013", "\\u0014", "\\u0015", "\\u0016", "\\u0017",      
            "\\u0018", "\\u0019", "\\u001A", "\\u001B", "\\u001C", "\\u001D", "\\u001E", "\\u001F",      
    };
    public static void escape(Appendable appendable,String toEscape) throws IOException {
        for( char c : toEscape.toCharArray() ) {
            switch( c ) {
            case '\\':
                appendable.append('\\').append('\\');
                break;
            case '/':
                appendable.append('\\').append('/');
                break;
            case 0x0008:
                appendable.append('\\').append('b');
                break;
            case 0x000c:
                appendable.append('\\').append('f');
                break;
            case 0x000a:
                appendable.append('\\').append('n');
                break;
            case 0x000d:
                appendable.append('\\').append('r');
                break;
            case 0x0009:
                appendable.append('\\').append('t');
                break;
            case '"':
                appendable.append('\\').append('"');
                break;
            default:
                if ( c >= 0x20 ) {
                    appendable.append(c);
                } else {
                    appendable.append(hexCode[c]);
                }
            }
        }
    }

    public static String unescape(String quotedString) {
        StringBuilder sb = new StringBuilder(quotedString.length()-2);
        char[] charArray = quotedString.toCharArray();
        boolean inEscape = false;
        int unicodeCount = 0;
        char uc = 0;
        for (int i=1; i<charArray.length-1; i++) {
            char c = charArray[i];
            if (inEscape) {
                switch(c) {
                case '\\': sb.append('\\'); break;
                case '/':  sb.append('/');  break;
                case 'b':  sb.append('\b'); break;
                case 'f':  sb.append('\f'); break;
                case 'n':  sb.append('\n'); break;
                case 'r':  sb.append('\r'); break;
                case 't':  sb.append('\t'); break;
                case '"':  sb.append('"');  break;
                case 'u':  unicodeCount = 4; break;
                }
                inEscape = false;
            } else if (unicodeCount != 0) {
                uc <<= 4;
                uc += fromHex(c);
                unicodeCount --;
                if (unicodeCount == 0) {
                    sb.append(uc);
                    uc = 0;
                }
            } else if (c == '\\') {
                inEscape = true;
            } else { 
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private static char fromHex(char c) {
        switch(c) {
        case '0': return 0;
        case '1': return 1;
        case '2': return 2;
        case '3': return 3;
        case '4': return 4;
        case '5': return 5;
        case '6': return 6;
        case '7': return 7;
        case '8': return 8;
        case '9': return 9;
        case 'a':
        case 'A': return 10;
        case 'b':
        case 'B': return 11;
        case 'c':
        case 'C': return 12;
        case 'd':
        case 'D': return 13;
        case 'e':
        case 'E': return 14;
        case 'f':
        case 'F': return 15;
        }
        return 0;
    }
}
