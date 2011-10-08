// NumberPrototype.java
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


import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.RangeError;
import FESI.Interpreter.Evaluator;

/**
 * Implements the prototype and is the class of all Number objects
 */
class NumberPrototype extends ESObject {
    private static final long serialVersionUID = 5851526384066944881L;
    // The value
    protected ESNumber value = ESNumber.valueOf(0.0);

    /**
     * Create a new un-initialzed Number
     */
    NumberPrototype(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }

    // overrides
    @Override
    public String getESClassName() {
        return "Number";
    }

    // overrides
    @Override
    public boolean isNumberValue() {
        return true;
    }

    // overrides
    @Override
    public double doubleValue() {
        return value.doubleValue();
    }

    // overrides
    @Override
    public boolean booleanValue() {
        return value.booleanValue();
    }

    // overrides
    @Override
    public String toString() {
        if (value == null)
            return super.toString();
        return value.toString();
    }

    public String toExponential(ESValue fractionalDigits) throws EcmaScriptException {
        double d = doubleValue();
        int f = fractionalDigits.toInt32();
        if (Double.isNaN(d)) {
            return "NaN";
        }
        StringBuilder sb = new StringBuilder();
        if (d < 0) {
            sb.append('-');
            d = -d;
        }
        if (Double.isInfinite(d)) {
            sb.append("Infinity");
        } else {
            boolean fractionDigitsDefined = fractionalDigits.getTypeOf() != EStypeUndefined;
            if (fractionDigitsDefined
                    && ( f< 0 || f > 20) ) {
                throw new RangeError("toExponential: fractionDigits must lie between 0 and 20 inclusive");
            }
            if (d == 0.0) {
                sb.append("0");
                if (f > 0) {
                    sb.append('.');
                    ESNumber.pad(sb, f, '0');
                }
                sb.append("e+0");
            } else {
                int n = (int)Math.floor(Math.log10(d));
                double sd = (d/Math.pow(10, n))/10.0d;
                String s;
                if (fractionDigitsDefined) {
                    s = ESNumber.toString(sd,f+1);
                    s = s.substring(2);
                } else {
                    s = ESNumber.toString(sd,16);
                    int distinctLength;
                    for ( distinctLength = s.length()-1; s.charAt(distinctLength) == '0'; distinctLength --) {
                        //
                    }
                    s = s.substring(2, distinctLength+1);
                }
                int k = s.length();
                if (k == 1) {
                    sb.append(s);
                } else {
                    sb.append(s.charAt(0)).append('.').append(s.substring(1));
                }
                sb.append('e');
                if (n >= 0) {
                    sb.append('+');
                }
                sb.append(n);
            }
        }
        return sb.toString();

    }

    // overrides
    @Override
    public ESValue toESNumber() throws EcmaScriptException {
        return value;
    }

    // overrides
    @Override
    public Object toJavaObject() {
        return new Double(value.doubleValue());
    }

    // overrides
    @Override
    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + ((value == null) ? "null" : value.toString()) + "]";
    }

    /**
     * Implementation of toString with precision.
     * The EcmaScript spec differs slightly from the Java implementation,
     * particularly leading zeros on the exponent.
     * Note that value should already have been checked to ensure it is
     * not NaN or Infinity 
     * @param precision Should be in range 1-21 inclusive
     * @return String representation of value to precision specified
     * @throws RangeError 
     */
    public String toString(int p) throws RangeError {
        double x = value.doubleValue();
        StringBuilder s = new StringBuilder();
        if (x < 0.0) {
            s.append('-');
            x = -x;
        }
        int e;
        if (x == 0.0) {
            ESNumber.pad(s, p, '0');
            e = 0;
        } else {
            double log10 = Math.log10(x);
            e = (int) Math.floor(log10);
            double n = x/Math.pow(10, e - p + 1);
            String m = ESNumber.toString(n,p);
            if (e < -6 || e >= p) {
                s.append(m.charAt(0));
                // This check is not in the spec. However all other implementations
                // appear not output a trailing period.
                if (p > 1) {
                    s.append('.').append(m.substring(1,p));
                }
                s.append('e');
                if (e>0) {
                    s.append('+');
                }
                s.append(e);
            } else if (e == (p-1)) {
                s.append(m);
            } else if (e >= 0) {
                s.append(m.substring(0,(e+1)));
                // This check is not in the spec. However all other implementations
                // appear not output a trailing period.
                if ((e+1) < m.length()) {
                    s.append('.').append(m.substring(e+1));
                }
            } else {
                s.append("0.");
                ESNumber.pad(s, -(e+1), '0');
                s.append(m);
            }
        }
        return s.toString();
    }
    
    public String toFixed(int fractionDigits) {
        double d = doubleValue();
        
        StringBuilder s = new StringBuilder(21);
        
        if (d >= 1e21 || Double.isNaN(d)) {
            return toString();
        }
        if (d < 0) {
            s.append('-');
            d = -d;
        }
        if (d > 1) {
            s.append(Long.toString((long)d));
        } else {
            s.append('0');
        }
        if (fractionDigits > 0) {
            s.append('.');
            long n = Math.round((d- (long)d) * Math.pow(10, fractionDigits));
            String afterPoint = Long.toString(n);
            int k = afterPoint.length();
            if (k < fractionDigits) {
                ESNumber.pad(s,fractionDigits-k,'0');
            }
            s.append(afterPoint);
        }
        return s.toString();
    }
      
}
