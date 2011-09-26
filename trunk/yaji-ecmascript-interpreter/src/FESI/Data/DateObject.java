// DateObject.java
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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Interpreter.Evaluator;

public class DateObject extends BuiltinFunctionObject {

    private static final long serialVersionUID = -4299764407542326480L;

    protected DateObject(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator, "Date", 7);
    }

    // overrides
    @Override
    public String toString() {
        return "<Date>";
    }

    // overrides
    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        return new ESString(new Date().toString());
    }

    // overrides
    @Override
    public ESObject doConstruct(ESValue[] arguments)
            throws EcmaScriptException {
        DatePrototype theObject = null;
        ESObject dp = getEvaluator().getDatePrototype();
        theObject = new DatePrototype(dp, getEvaluator());
        initialiseFromArguments(theObject, arguments);
        return theObject;
    }

    protected void initialiseFromArguments(DatePrototype theObject,
            ESValue[] arguments) throws EcmaScriptException {
        int l = arguments.length;

        if (l == 2 || l == 0) {
            theObject.date = new Date();
        } else if (l == 1) {
            double d = arguments[0].doubleValue();
            if (Double.isNaN(d)) {
                theObject.date = null;
            } else {
                theObject.date = new Date((long) d);
            }
        } else {
            int year = arguments[0].toInt32();
            if (0 <= year && year <= 99) {
                year += 1900;
            }
            int month = arguments[1].toInt32();
            int day = arguments[2].toInt32();
            int hour = (l > 3) ? arguments[3].toInt32() : 0;
            int minute = (l > 4) ? arguments[4].toInt32() : 0;
            int second = (l > 5) ? arguments[5].toInt32() : 0;
            int ms = (l > 6) ? arguments[6].toInt32() : 0;
            // Using current current locale, set it to the specified time
            // System.out.println("YEAR IS " + year);
            GregorianCalendar cal = new GregorianCalendar(year, month, day,
                    hour, minute, second);
            if (ms != 0) {
                cal.set(Calendar.MILLISECOND, ms);
            }
            theObject.date = cal.getTime();
        }
    }

    /**
     * Utility function to create the single Date object
     * 
     * @param evaluator
     *            the Evaluator
     * @param objectPrototype
     *            The Object prototype attached to the evaluator
     * @param functionPrototype
     *            The Function prototype attached to the evaluator
     * 
     * @return the Date singleton
     */
    public static DateObject makeDateObject(Evaluator evaluator,
            ObjectPrototype objectPrototype, FunctionPrototype functionPrototype) {

        DatePrototype datePrototype = new DatePrototype(objectPrototype,
                evaluator);
        DateObject dateObject = new DateObject(functionPrototype, evaluator);

        addBuiltInFunctions(dateObject, evaluator, functionPrototype,
                datePrototype);

        evaluator.setDatePrototype(datePrototype);

        return dateObject;
    }

    protected static void addBuiltInFunctions(DateObject dateObject,
            Evaluator evaluator, FunctionPrototype functionPrototype,
            DatePrototype datePrototype) throws ProgrammingError {
        try {

            // For datePrototype
            class DatePrototypeToString extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeToString(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return (aDate.date == null) ? new ESString("NaN")
                            : new ESString(aDate.date.toString());
                }
            }

            class DatePrototypeValueOf extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeValueOf(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    if (aDate.date == null) {
                        return ESNumber.valueOf(Double.NaN);
                    }
                    long t = aDate.date.getTime();
                    return ESNumber.valueOf(t);

                }
            }

            class DatePrototypeToLocaleString extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeToLocaleString(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    DateFormat df = DateFormat.getDateTimeInstance();
                    df.setTimeZone(TimeZone.getDefault());
                    return (aDate.date == null) ? new ESString("NaN")
                            : new ESString(df.format(aDate.date));
                }
            }

            class DatePrototypeToGMTString extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeToGMTString(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    DateFormat df = DateFormat.getDateTimeInstance(
                            DateFormat.MEDIUM, DateFormat.FULL);
                    df.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return (aDate.date == null) ? new ESString("NaN")
                            : new ESString(df.format(aDate.date));
                }
            }

            class DatePrototypeGetYear extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetYear(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    ESValue v = aDate.get(Calendar.YEAR);
                    return ESNumber.valueOf(v.doubleValue() - 1900);
                }
            }

            class DatePrototypeGetFullYear extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetFullYear(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.get(Calendar.YEAR);
                }
            }

            class DatePrototypeGetUTCFullYear extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetUTCFullYear(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.getUTC(Calendar.YEAR);
                }
            }

            class DatePrototypeGetMonth extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetMonth(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.get(Calendar.MONTH);
                }
            }

            class DatePrototypeGetUTCMonth extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetUTCMonth(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.getUTC(Calendar.MONTH);
                }
            }

            class DatePrototypeGetDate extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetDate(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.get(Calendar.DAY_OF_MONTH);
                }
            }

            class DatePrototypeGetUTCDate extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetUTCDate(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.getUTC(Calendar.DAY_OF_MONTH);
                }
            }

            class DatePrototypeGetDay extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetDay(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    // EcmaScript has SUNDAY=0, java SUNDAY=1 - converted in
                    // DatePrototype
                    return aDate.get(Calendar.DAY_OF_WEEK);
                }
            }

            class DatePrototypeGetUTCDay extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetUTCDay(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.getUTC(Calendar.DAY_OF_WEEK);
                }
            }

            class DatePrototypeGetHours extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetHours(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.get(Calendar.HOUR_OF_DAY);
                }
            }

            class DatePrototypeGetUTCHours extends BuiltinFunctionObject {

                private static final long serialVersionUID = 1L;

                DatePrototypeGetUTCHours(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.getUTC(Calendar.HOUR_OF_DAY);
                }
            }

            class DatePrototypeGetMinutes extends BuiltinFunctionObject {

                private static final long serialVersionUID = 1L;

                DatePrototypeGetMinutes(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.get(Calendar.MINUTE);
                }
            }

            class DatePrototypeGetUTCMinutes extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetUTCMinutes(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.getUTC(Calendar.MINUTE);
                }
            }

            class DatePrototypeGetSeconds extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetSeconds(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.get(Calendar.SECOND);
                }
            }

            class DatePrototypeGetUTCSeconds extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetUTCSeconds(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.getUTC(Calendar.SECOND);
                }
            }

            class DatePrototypeGetMilliseconds extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetMilliseconds(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.get(Calendar.MILLISECOND);
                }
            }

            class DatePrototypeGetUTCMilliseconds extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetUTCMilliseconds(String name,
                        Evaluator evaluator, FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.getUTC(Calendar.MILLISECOND);
                }
            }

            class DatePrototypeSetYear extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetYear(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setYear(arguments);
                }
            }

            class DatePrototypeSetFullYear extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetFullYear(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setTime(arguments, new int[] { Calendar.YEAR,
                            Calendar.MONTH, Calendar.DAY_OF_MONTH });
                }
            }

            class DatePrototypeSetUTCFullYear extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetUTCFullYear(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setUTCTime(arguments, new int[] {
                            Calendar.YEAR, Calendar.MONTH,
                            Calendar.DAY_OF_MONTH });
                }
            }

            class DatePrototypeSetMonth extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetMonth(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setTime(arguments, new int[] { Calendar.MONTH,
                            Calendar.DAY_OF_MONTH });
                }
            }

            class DatePrototypeSetUTCMonth extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetUTCMonth(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setUTCTime(arguments, new int[] {
                            Calendar.MONTH, Calendar.DAY_OF_MONTH });
                }
            }

            class DatePrototypeSetDate extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetDate(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setTime(arguments,
                            new int[] { Calendar.DAY_OF_MONTH });
                }
            }

            class DatePrototypeSetUTCDate extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetUTCDate(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setUTCTime(arguments,
                            new int[] { Calendar.DAY_OF_MONTH });
                }
            }

            class DatePrototypeSetHours extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetHours(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setTime(arguments, new int[] {
                            Calendar.HOUR_OF_DAY, Calendar.MINUTE,
                            Calendar.SECOND, Calendar.MILLISECOND });
                }
            }

            class DatePrototypeSetUTCHours extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetUTCHours(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setUTCTime(arguments, new int[] {
                            Calendar.HOUR_OF_DAY, Calendar.MINUTE,
                            Calendar.SECOND, Calendar.MILLISECOND });
                }
            }

            class DatePrototypeSetMinutes extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetMinutes(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setTime(arguments, new int[] {
                            Calendar.MINUTE, Calendar.SECOND,
                            Calendar.MILLISECOND });
                }
            }

            class DatePrototypeSetUTCMinutes extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetUTCMinutes(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setUTCTime(arguments, new int[] {
                            Calendar.MINUTE, Calendar.SECOND,
                            Calendar.MILLISECOND });
                }
            }

            class DatePrototypeSetSeconds extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetSeconds(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setTime(arguments, new int[] {
                            Calendar.SECOND, Calendar.MILLISECOND });
                }
            }

            class DatePrototypeSetUTCSeconds extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetUTCSeconds(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setUTCTime(arguments, new int[] {
                            Calendar.SECOND, Calendar.MILLISECOND });
                }
            }

            class DatePrototypeSetMilliseconds extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetMilliseconds(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setTime(arguments,
                            new int[] { Calendar.MILLISECOND });
                }
            }

            class DatePrototypeSetUTCMilliseconds extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetUTCMilliseconds(String name,
                        Evaluator evaluator, FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    return aDate.setUTCTime(arguments,
                            new int[] { Calendar.MILLISECOND });
                }
            }

            class DatePrototypeGetTimezoneOffset extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeGetTimezoneOffset(String name,
                        Evaluator evaluator, FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    GregorianCalendar cal = new GregorianCalendar(TimeZone
                            .getDefault());
                    cal.setTime(aDate.date);
                    TimeZone tz = cal.getTimeZone();

                    int millis = cal.get(Calendar.MILLISECOND)
                            + cal.get(Calendar.SECOND) * 1000
                            + cal.get(Calendar.MINUTE) * 60 * 1000
                            + cal.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;

                    int offset = tz.getOffset(cal.get(Calendar.ERA), cal
                            .get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                            .get(Calendar.DAY_OF_MONTH), cal
                            .get(Calendar.DAY_OF_WEEK), millis);

                    // int offset = TimeZone.getDefault().getRawOffset();
                    // System.out.println("TimeZone.getDefault().getID(): " +
                    // TimeZone.getDefault().getID());
                    // System.out.println("TimeZone.getDefault().getRawOffset(): "
                    // + TimeZone.getDefault().getRawOffset());

                    int minutes = -(offset / 1000 / 60); // convert to minutes
                    return ESNumber.valueOf(minutes);
                }
            }

            class DatePrototypeSetTime extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DatePrototypeSetTime(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 0);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    DatePrototype aDate = (DatePrototype) thisObject;
                    double dateValue = Double.NaN;
                    if (arguments.length > 0) {
                        dateValue = arguments[0].doubleValue();
                    }
                    if (Double.isNaN(dateValue)) {
                        aDate.setDate(null);
                    } else {
                        aDate.setDate(new Date((long) dateValue));
                    }
                    return ESNumber.valueOf(dateValue);
                }
            }

            // For dateObject
            class DateObjectParse extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DateObjectParse(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 1);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    if (arguments.length <= 0) {
                        throw new EcmaScriptException("Missing argument");
                    }
                    String dateString = arguments[0].toString();
                    DateFormat df = DateFormat.getDateInstance();
                    ESValue dateValue = null;
                    try {
                        Date date = df.parse(dateString);
                        dateValue = ESNumber.valueOf(date.getTime());
                    } catch (java.text.ParseException e) {
                        dateValue = ESNumber.valueOf(Double.NaN);
                    }
                    return dateValue;
                }
            }

            class DateObjectUTC extends BuiltinFunctionObject {
                private static final long serialVersionUID = 1L;

                DateObjectUTC(String name, Evaluator evaluator,
                        FunctionPrototype fp) {
                    super(fp, evaluator, name, 7);
                }

                @Override
                public ESValue callFunction(ESValue thisObject,
                        ESValue[] arguments) throws EcmaScriptException {
                    int l = arguments.length;
                    if (l <= 2) {
                        throw new EcmaScriptException("Missing argument");
                    }
                    int year = arguments[0].toInt32();
                    if (0 <= year && year <= 99) {
                        year += 1900;
                    }
                    int month = arguments[1].toInt32();
                    int day = arguments[2].toInt32();
                    int hour = (l > 3) ? arguments[3].toInt32() : 0;
                    int minute = (l > 4) ? arguments[4].toInt32() : 0;
                    int second = (l > 5) ? arguments[5].toInt32() : 0;
                    int ms = (l > 6) ? arguments[6].toInt32() : 0;
                    Calendar cal = new GregorianCalendar(TimeZone
                            .getTimeZone("GMT"));
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month);
                    cal.set(Calendar.DAY_OF_MONTH, day);
                    cal.set(Calendar.HOUR_OF_DAY, hour);
                    cal.set(Calendar.MINUTE, minute);
                    cal.set(Calendar.SECOND, second);
                    cal.set(Calendar.MILLISECOND, ms);
                    long timeinms = cal.getTime().getTime();
                    return ESNumber.valueOf(timeinms);
                }
            }

            dateObject.putHiddenProperty("prototype", datePrototype);
            dateObject.putHiddenProperty("length", ESNumber.valueOf(7));
            dateObject.putHiddenProperty("parse", new DateObjectParse("parse",
                    evaluator, functionPrototype));
            dateObject.putHiddenProperty("UTC", new DateObjectUTC("UTC",
                    evaluator, functionPrototype));

            datePrototype.putHiddenProperty("constructor", dateObject);
            datePrototype.putHiddenProperty("toString",
                    new DatePrototypeToString("toString", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("toLocaleString",
                    new DatePrototypeToLocaleString("toLocaleString",
                            evaluator, functionPrototype));
            datePrototype.putHiddenProperty("toGMTString",
                    new DatePrototypeToGMTString("toGMTString", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("toUTCString",
                    new DatePrototypeToGMTString("toUTCString", evaluator,
                            functionPrototype));

            datePrototype.putHiddenProperty("valueOf",
                    new DatePrototypeValueOf("valueOf", evaluator,
                            functionPrototype));

            datePrototype.putHiddenProperty("getTime",
                    new DatePrototypeValueOf("getTime", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getYear",
                    new DatePrototypeGetYear("getYear", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getFullYear",
                    new DatePrototypeGetFullYear("getFullYear", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getUTCFullYear",
                    new DatePrototypeGetUTCFullYear("getUTCFullYear",
                            evaluator, functionPrototype));
            datePrototype.putHiddenProperty("getMonth",
                    new DatePrototypeGetMonth("getMonth", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getUTCMonth",
                    new DatePrototypeGetUTCMonth("getUTCMonth", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getDate",
                    new DatePrototypeGetDate("getDate", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getUTCDate",
                    new DatePrototypeGetUTCDate("getUTCDate", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getDay", new DatePrototypeGetDay(
                    "getDay", evaluator, functionPrototype));
            datePrototype.putHiddenProperty("getUTCDay",
                    new DatePrototypeGetUTCDay("getUTCDay", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getHours",
                    new DatePrototypeGetHours("getHours", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getUTCHours",
                    new DatePrototypeGetUTCHours("getUTCHours", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getMinutes",
                    new DatePrototypeGetMinutes("getMinutes", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getUTCMinutes",
                    new DatePrototypeGetUTCMinutes("getUTCMinutes", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getSeconds",
                    new DatePrototypeGetSeconds("getSeconds", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getUTCSeconds",
                    new DatePrototypeGetUTCSeconds("getUTCSeconds", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("getMilliseconds",
                    new DatePrototypeGetMilliseconds("getMilliseconds",
                            evaluator, functionPrototype));
            datePrototype.putHiddenProperty("getUTCMilliseconds",
                    new DatePrototypeGetUTCMilliseconds("getUTCMilliseconds",
                            evaluator, functionPrototype));

            datePrototype.putHiddenProperty("setYear",
                    new DatePrototypeSetYear("setYear", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setFullYear",
                    new DatePrototypeSetFullYear("setFullYear", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setUTCFullYear",
                    new DatePrototypeSetUTCFullYear("setUTCFullYear",
                            evaluator, functionPrototype));
            datePrototype.putHiddenProperty("setMonth",
                    new DatePrototypeSetMonth("setMonth", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setUTCMonth",
                    new DatePrototypeSetUTCMonth("setUTCMonth", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setDate",
                    new DatePrototypeSetDate("setDate", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setUTCDate",
                    new DatePrototypeSetUTCDate("setUTCDate", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setHours",
                    new DatePrototypeSetHours("setHours", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setUTCHours",
                    new DatePrototypeSetUTCHours("setUTCHours", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setMinutes",
                    new DatePrototypeSetMinutes("setMinutes", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setUTCMinutes",
                    new DatePrototypeSetUTCMinutes("setUTCMinutes", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setSeconds",
                    new DatePrototypeSetSeconds("setSeconds", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setUTCSeconds",
                    new DatePrototypeSetUTCSeconds("setUTCSeconds", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("setMilliseconds",
                    new DatePrototypeSetMilliseconds("setMilliseconds",
                            evaluator, functionPrototype));
            datePrototype.putHiddenProperty("setUTCMilliseconds",
                    new DatePrototypeSetUTCMilliseconds("setUTCMilliseconds",
                            evaluator, functionPrototype));

            datePrototype.putHiddenProperty("getTimezoneOffset",
                    new DatePrototypeGetTimezoneOffset("getTimezoneOffset",
                            evaluator, functionPrototype));

            datePrototype.putHiddenProperty("setTime",
                    new DatePrototypeSetTime("setTime", evaluator,
                            functionPrototype));

        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }
    }
}
