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

/**
 * Implements the string primitive value
 */
public final class ESString extends ESStringPrimitive {
    private static final long serialVersionUID = 2155563970150911900L;

    private String string;

    /**
     * Create a new value from the string parameters
     * 
     * @param value
     *            The immutable value
     */
    public ESString(String value) {
        this.string = value;
    }

    @Override
    public String toString() {
        return string;
    }

    /**
     * Returns the length of the string
     * 
     * @return the length of the string
     */
    @Override
    public int getStringLength() {
        return string.length();
    }

    // overrides
    @Override
    public ESValue toESString() {
        return this;
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
