// ObjectPrototype.java
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
 * Implements the prototype and is the class of all Object objects.
 * <P>
 * All functionality of objects is implemented in the superclass ESObject.
 */
public class ObjectPrototype extends ESObject {

    private static final long serialVersionUID = -4836187608032396557L;

    /**
     * Create a new Object with a specific prototype. This should be used by
     * routine implementing object with another prototype than Object. To create
     * an EcmaScript Object use ObjectObject.createObject()
     * 
     * @param prototype
     *            the prototype of the new object
     * @param evaluator
     *            The evaluator
     */
    public ObjectPrototype(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }
    
    @Override
    public boolean canJson() {
        return true;
    }

    @Override
    public void toJson(Appendable appendable, JsonState state, String parentPropertyName) throws IOException, EcmaScriptException {
        state.pushCyclicCheck(this);
        ESValue toJsonFunction = getPropertyIfAvailable(StandardProperty.TOJSONstring, StandardProperty.TOJSONhash);
        if (toJsonFunction != null && toJsonFunction instanceof FunctionPrototype) {
            ESValue value = toJsonFunction.callFunction(this, new ESValue[] { ESString.valueOf(parentPropertyName) });
            if (value instanceof ObjectPrototype) {
                ((ObjectPrototype)value).toJsonString(appendable, state);
            } else {
                value.toJson(appendable, state, parentPropertyName);
            }
        } else {
            toJsonString(appendable, state);
        }
        state.popCyclicCheck();
    }

    private void toJsonString(Appendable appendable, JsonState state) throws IOException, EcmaScriptException {
        appendable.append('{');
        if (!hasNoPropertyMap()) {
            getPropertyMap().toJson(appendable, state, this);
        }
        appendable.append('}');
    }

}