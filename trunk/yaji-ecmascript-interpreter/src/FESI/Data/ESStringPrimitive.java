package FESI.Data;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public abstract class ESStringPrimitive extends ESPrimitive {

    private static final long serialVersionUID = -934231636489120309L;

    @Override
    public String toDetailString() {
        return "ES:\"" + toString() + "\"";
    }

    @Override
    public int getTypeOf() {
        return EStypeString;
    }

    @Override
    public String getTypeofString() {
        return "string";
    }

    @Override
    public double doubleValue() {
        double value;
        try {
            // Will accept leading / trailing spaces, unlike new Integer !
            String str = trim(toString());
            if (str.length() == 0) {
                value = 0.0;
            } else if (str.length() > 2 && str.charAt(0) == '0' && isX(str.charAt(1)) ) {
                value = Integer.decode(str).doubleValue();
            } else {
                value = (Double.valueOf(str)).doubleValue();
            }
        } catch (NumberFormatException e) {
            value = Double.NaN;
        }
        return value;
    }

    private boolean isX(char charAt) {
        return charAt == 'x' || charAt == 'X';
    }

    static String trim(String string) {
        char[] s = string.toCharArray();
        int start = 0; 
        while (start < s.length && isWhitespace(s[start])) {
            start++;
        }
        int end=s.length-1;
        while (end > start && isWhitespace(s[end])) {
            end --;
        }
        int length = end-start+1;
        String value = (length == s.length)?string:new String(s,start,length);
        return value;
    }

    private static boolean isWhitespace(char c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c) || c == '\uFEFF';
    }

    @Override
    public boolean isStringValue() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        // handle the special cases of ESNull and ESUndefined
        if (o == ESNull.theNull || o == ESUndefined.theUndefined) {
            return false;
        }

        if (o instanceof ESValue) {
            String thisString = this.toString();
            String otherString = o.toString();
            return thisString == null ? otherString == null : thisString
                    .equals(otherString);
        }

        return false;
    }

    @Override
    public boolean equalsSameType(ESValue v2) throws EcmaScriptException {
        String s1 = toString();
        String s2 = v2.toString();
        return s1.equals(s2);
    }

    @Override
    public boolean booleanValue() {
        return getStringLength() > 0;
    }

    public abstract int getStringLength();

    @Override
    public ESObject toESObject(Evaluator evaluator) throws EcmaScriptException {
        StringPrototype theObject = null;
        ESObject sp = evaluator.getStringPrototype();
        theObject = new StringPrototype(sp, evaluator);
        theObject.value = (ESString) toESString();
        return theObject;
    }

    @Override
    public Object toJavaObject() {
        return toString();
    }

    /**
     * Advanced FESI
     * GT Modified: 5/10/2004
     * Support for subtypes (storing values in hashset)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // equates null and the empty string(!)
        String temp = toString();
        return temp != null ? temp.hashCode() : 0;
    }


}
