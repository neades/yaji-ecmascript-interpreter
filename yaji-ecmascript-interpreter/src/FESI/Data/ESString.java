// ESString.java
// FESI Copyright (c) Jean-Marc Lugrin, 1999
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package FESI.Data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.yaji.json.JsonState;
import org.yaji.json.JsonUtil;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import FESI.Util.IAppendable;

/**
 * Implements the string primitive value
 */
public final class ESString extends ESPrimitive {
    private static final long serialVersionUID = 2155563970150911900L;

    // The value is only held in the string until we get an appendBuffer. After
    // we get our
    // appendBuffer, we never go back to storing it in the string.
    private String string;
    private IAppendable appendBuffer;

    /**
     * Create a new value from the string parameters
     * 
     * @param value
     *            The immutable value
     */
    public ESString(String value) {
        this.string = value;
        this.appendBuffer = null;
    }

    // We assume the caller will not modify the appendBuffer after giving it to
    // us.
    public ESString(IAppendable appendBuffer) {
        this.string = null;
        this.appendBuffer = appendBuffer;
    }

    // overrides
    @Override
    public String toDetailString() {
        return "ES:\"" + toString() + "\"";
    }

    // overrides
    @Override
    public int getTypeOf() {
        return EStypeString;
    }

    // overrides
    @Override
    public String getTypeofString() {
        return "string";
    }

    // RID 27460: ESString should be immutable and this method is the only one
    // that changes it.
    // It is only currently used for the "result" string within representations
    // when representation
    // optimisation is enabled.
    public void appendString(ESValue appendage, Evaluator evaluator) {
        if (appendBuffer == null) {
            appendBuffer = evaluator.createAppendable(2, string.length());

            appendBuffer.append(string);

            string = null;
        }

        if (appendage instanceof ESString) {
            IAppendable otherAppendBuffer = ((ESString) appendage).appendBuffer;

            if (otherAppendBuffer != null) {
                appendBuffer.append(otherAppendBuffer);
            } else {
                appendBuffer.append(((ESString) appendage).string);
            }

            return;
        }

        appendBuffer.append(appendage.toString());
    }

    // We will not give access to our appendBuffer, but we will offer to append
    // it to somebody
    // else's IAppendable.
    public void appendSelfToAppendable(IAppendable appendable) {
        if (appendBuffer != null) {
            appendable.append(appendBuffer);
        } else {
            appendable.append(string);
        }
    }

    // overrides
    @Override
    public String toString() {
        if (appendBuffer != null) {
            return appendBuffer.toString();
        }

        return string;
    }

    /**
     * Returns the length of the string
     * 
     * @return the length of the string
     */
    public int getStringLength() {
        if (appendBuffer != null) {
            return appendBuffer.length();
        }

        return string.length();
    }

    // overrides
    @Override
    public double doubleValue() {
        double value;
        try {
            // Will accept leading / trailing spaces, unlike new Integer !
            String str = trim(toString());
            if (str.length() == 0) {
                value = 0.0;
            } else if (str.startsWith("0x")) {
                value = Integer.decode(str).doubleValue();
            } else {
                value = (Double.valueOf(str)).doubleValue();
            }
        } catch (NumberFormatException e) {
            value = Double.NaN;
        }
        return value;
    }

    // overrides
    @Override
    public boolean booleanValue() {
        return getStringLength() > 0;
    }

    // overrides
    @Override
    public ESValue toESString() {
        return this;
    }

    // overrides
    @Override
    public ESObject toESObject(Evaluator evaluator) throws EcmaScriptException {
        StringPrototype theObject = null;
        ESObject sp = evaluator.getStringPrototype();
        theObject = new StringPrototype(sp, evaluator);
        theObject.value = this;
        return theObject;
    }

    // overrides
    @Override
    public Object toJavaObject() {
        return toString();
    }

    // overrides
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

    /**
     * Advanced FESI GT Modified: 5/10/2004 Support for subtypes (storing values
     * in hashset)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        // equates null and the empty string(!)
        String temp = toString();
        return temp != null ? temp.hashCode() : 0;
    }

    @Override
    public boolean equalsSameType(ESValue v2) throws EcmaScriptException {
        String s1 = toString();
        String s2 = v2.toString();
        return s1.equals(s2);
    }

    private static Map<String, ESString> cache = Collections
            .synchronizedMap(new HashMap<String, ESString>());

    public static ESString valueOf(String value) {
        if (value == null) {
            return null;
        }
        ESString result = cache.get(value);
        if (result == null) {
            String internedValue = value.intern();
            result = new ESString(internedValue);
            cache.put(internedValue, result);
        }
        return result;
    }
    
    @Override
    public void toJson(Appendable appendable, JsonState state, String parentPropertyName) throws IOException {
        appendable.append('"');
        JsonUtil.escape(appendable,toString());
        appendable.append('"');
    }

    @Override
    public boolean canJson() {
        return true;
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

    public static ESValue valueOf(int i) {
        String str = Integer.toString(i);
        if (i < 256) {
            return valueOf(str);
        }
        return new ESString(str);
    }

    @Override
    protected boolean sameValueTypeChecked(ESValue other)
            throws EcmaScriptException {
        return toString().equals(other.toString());
    }
}
