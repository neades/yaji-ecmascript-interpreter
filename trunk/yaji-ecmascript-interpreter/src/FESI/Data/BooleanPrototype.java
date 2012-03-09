// BooleanPrototype.java
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

import org.yaji.json.JsonState;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

/**
 * Implements the prototype and is the class of all Boolean objects
 */
class BooleanPrototype extends ESObject {
    private static final long serialVersionUID = 6495065713332027767L;
    // The value
    protected ESBoolean value = ESBoolean.valueOf(false);

    /**
     * Create a new Boolean initialized to false
     * 
     * @param prototype
     *            the BooleanPrototype
     * @param evaluator
     *            the evaluator
     */
    BooleanPrototype(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }

    // overrides
    @Override
    public String getESClassName() {
        return "Boolean";
    }

    // overrides
    @Override
    public double doubleValue() throws EcmaScriptException {
        return value.doubleValue();
    }

    // overrides
    @Override
    public Object toJavaObject() {
        return Boolean.valueOf(value.booleanValue());
    }

    // overrides
    @Override
    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + ((value == null) ? "null" : value.toString()) + "]";
    }

    // overrides
    @Override
    public boolean isBooleanValue() {
        return true;
    }

    @Override
    public boolean canJson() {
        return true;
    }
    
    @Override
    public void toJson(Appendable appendable, JsonState state,
            String parentPropertyName) throws IOException, EcmaScriptException {
        value.toJson(appendable, state, parentPropertyName);
    }
}
