/**
 * This is used by representations so that they can build up a large string with many appends without generating too
 * much garbage.
 *
 * This class should not be used when the average size of strings being appended is less than 5.
 *
 * It is effectively an ESString.
 */

package FESI.Data;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import FESI.Util.IAppendable;

public final class ESAppendable extends ESStringPrimitive {
    private static final long serialVersionUID = -759780794184427107L;
    private IAppendable appendBuffer;

    // We assume ownership of appendBuffer and that the caller will not subsequently modify it.
    public ESAppendable(IAppendable appendBuffer) {
        this.appendBuffer = appendBuffer;
    }

    public void appendString(ESValue appendage, Evaluator evaluator) {
        if (appendBuffer == null) {
            appendBuffer = evaluator.createAppendable(256, 4096);
        }

        if (appendage instanceof ESAppendable) {
            IAppendable otherAppendBuffer = ((ESAppendable) appendage).appendBuffer;

            if (otherAppendBuffer != null) {
                appendBuffer.append(otherAppendBuffer);
                return;
            }
        }

        appendBuffer.append(appendage.toString());
    }

    // We will not give access to our appendBuffer, but we will offer to append it to somebody
    // else's IAppendable.
    public void appendSelfToAppendable(IAppendable appendable) {
        if (appendBuffer != null) {
            appendable.append(appendBuffer);
        } else {
            appendable.append(toString());
        }
    }

    @Override
    public String toString() {
        if (appendBuffer == null) {
            return null;
        }

        return appendBuffer.toString();
    }

    /**
     * Returns the length of the string
     *
     * @return the length of the string
     */
    @Override
    public int getStringLength() {
        return appendBuffer.length();
    }

    @Override
    public ESValue toESString() {
        if (appendBuffer == null) {
            return ESNull.theNull;
        }

        return new ESString(appendBuffer.toString());
    }

    @Override
    public boolean equalsSameType(ESValue v2) throws EcmaScriptException {
        String s1 = toString();
        String s2 = v2.toString();
        return s1.equals(s2);
    }

    @Override
    protected boolean sameValueTypeChecked(ESValue other)
            throws EcmaScriptException {
        return toString().equals(other.toString());
    }
}
