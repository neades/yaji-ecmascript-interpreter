// DatePrototype.java
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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

/**
 * Implements the prototype and is the class of all Date objects
 */
public class DatePrototype extends ESObject {
    private static final long serialVersionUID = -2916775600018274955L;
    // The value
    protected Date date = null;

    /**
     * Create a new Date object with a null date
     * 
     * @param prototype
     *            the Date prototype
     * @param evaluator
     *            the Evaluator
     */
    protected DatePrototype(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
        date = new Date();
    }

    /**
     * Create a new Date object with a specified date
     * 
     * @param evaluator
     *            the Evaluator
     * @param aDate
     *            the Date
     */
    public DatePrototype(Evaluator evaluator, Date aDate) {
        super(evaluator.getDatePrototype(), evaluator);
        date = new Date(aDate.getTime());
    }

    /**
     * Create a new Date object with a specified date
     * 
     * @param evaluator
     *            the Evaluator
     * @param time
     *            the Date
     */
    public DatePrototype(Evaluator evaluator, long time) {
        super(evaluator.getDatePrototype(), evaluator);
        date = new Date(time);
    }

    // overrides
    @Override
    public String getESClassName() {
        return "Date";
    }

    @Override
    public String getTypeofString() {
        return "Date";
    }

    /**
     * Set the year value of the date. BEWARE: Fixed as base 1900 !
     * 
     * @param arguments
     *            The array of arguments, the first one being the year
     * @return the new date as a number
     */
    public ESValue setYear(ESValue[] arguments) throws EcmaScriptException {
        if (date == null) {
            return ESNumber.valueOf(Double.NaN);
        }
        if (arguments.length <= 0) {
            date = null;
            return ESNumber.valueOf(Double.NaN);
        }
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
        cal.setTime(date);
        double d = arguments[0].doubleValue();
        if (Double.isNaN(d)) {
            date = null;
            return ESNumber.valueOf(Double.NaN);
        }
        if (d < 100) {
            d += 1900;
        }
        cal.set(Calendar.YEAR, (int) d);
        setDate(cal.getTime());
        long t = date.getTime();
        return ESNumber.valueOf(t);

    }

    /**
     * Set the time value of the date based on the element type to change Assume
     * that the time elements are in the local time zone
     * 
     * @param arguments
     *            The array of arguments
     * @para, argTypes The array of element type
     * @return the new date as a number
     */
    public ESValue setTime(ESValue[] arguments, int[] argTypes)
            throws EcmaScriptException {
        if (date == null) {
            return ESNumber.valueOf(Double.NaN);
        }
        if (arguments.length <= 0) {
            date = null;
            return ESNumber.valueOf(Double.NaN);
        }
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
        cal.setTime(date);
        for (int iarg = 0; (iarg < argTypes.length)
                && (iarg < arguments.length); iarg++) {
            double d = arguments[iarg].doubleValue();
            if (Double.isNaN(d)) {
                date = null;
                return ESNumber.valueOf(Double.NaN);
            }
            cal.set(argTypes[iarg], (int) d);
        }
        setDate(cal.getTime());
        long t = date.getTime();
        return ESNumber.valueOf(t);

    }

    protected void setDate(Date date) {
        this.date = date;
    }

    /**
     * Set the time value of the date based on the element type to change Assume
     * that the time elements are in the UTC time zone
     * 
     * @param arguments
     *            The array of arguments
     * @para, argTypes The array of element type
     * @return the new date as a number
     */
    public ESValue setUTCTime(ESValue[] arguments, int[] argTypes)
            throws EcmaScriptException {
        if (date == null) {
            return ESNumber.valueOf(Double.NaN);
        }
        if (arguments.length <= 0) {
            date = null;
            return ESNumber.valueOf(Double.NaN);
        }
        GregorianCalendar cal = new GregorianCalendar(TimeZone
                .getTimeZone("GMT"));
        cal.setTime(date);
        for (int iarg = 0; (iarg < argTypes.length)
                && (iarg < arguments.length); iarg++) {
            double d = arguments[iarg].doubleValue();
            if (Double.isNaN(d)) {
                date = null;
                return ESNumber.valueOf(Double.NaN);
            }
            cal.set(argTypes[iarg], (int) d);
        }
        setDate(cal.getTime());
        long t = date.getTime();
        return ESNumber.valueOf(t);

    }

    /**
     * Get an element of the date (in local time zone)
     * 
     * @param element
     *            The type of the element
     * @return the element as a value
     */
    public ESValue get(int element) {
        if (date == null) {
            return ESNumber.valueOf(Double.NaN);
        }
        GregorianCalendar cal = new GregorianCalendar(TimeZone.getDefault());
        cal.setTime(date);
        long t = cal.get(element);
        // EcmaScript has SUNDAY=0, java SUNDAY=1 - converted in DatePrototype
        if (element == Calendar.DAY_OF_WEEK) {
            t--;
        }
        return ESNumber.valueOf(t);

    }

    /**
     * Get an element of the date (in UTC time zone)
     * 
     * @param element
     *            The type of the element
     * @return the element as a value
     */
    public ESValue getUTC(int element) {
        if (date == null) {
            return ESNumber.valueOf(Double.NaN);
        }
        GregorianCalendar cal = new GregorianCalendar(TimeZone
                .getTimeZone("GMT"));
        cal.setTime(date);
        long t = cal.get(element);
        // EcmaScript has SUNDAY=0, java SUNDAY=1 - converted in DatePrototype
        if (element == Calendar.DAY_OF_WEEK) {
            t--;
        }
        return ESNumber.valueOf(t);

    }

    // overrides
    @Override
    public String toString() {
        return (date == null ? "null" : date.toString());
    }

    // overrides
    @Override
    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + ((date == null) ? "null" : date.toString()) + "]";
    }

    // overrides
    @Override
    public Object toJavaObject() {
        return date;
    }

    // overrides
    @Override
    public ESValue getDefaultValue() throws EcmaScriptException {
        return this.getDefaultValue(EStypeString);
    }

    @Override
    public int hashCode() {
        // equating null and java epoch start, January 1, 1970, 00:00:00 GMT (!)
        return date != null ? date.hashCode() : 0;
    }

    /**
     * Advanced FESI GT Modified: 12/4/2005 Support for subtypes
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof DatePrototype) {
            DatePrototype other = (DatePrototype) o;
            return date == null ? other.date == null : date.equals(other.date);
        }

        return false;
    }
}
