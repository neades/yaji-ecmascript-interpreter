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
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.ProgrammingError;
import FESI.Exceptions.RangeError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

public class DateObject extends BuiltinFunctionObject {

    private static final int YEAR_POSITION = 0;
    private static final int MONTH_POSITION = 1;
    private static final int DAY_POSITION = 2;
    private static final int HOUR_POSITION = 3;
    private static final int MINUTE_POSITION = 4;
    private static final int SECOND_POSITION = 5;
    private static final int MILLISECOND_POSITION = 6;

    private static final long serialVersionUID = -4299764407542326480L;
    
    private static final String SIMPLIFIED_ISO8601_DATE_FORMAT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    public static final String UTC_FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS zzz";
    
    public static final String TO_STRING_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";

    private static class DateObjectParse extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DateObjectParse(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            String dateString = getArg(arguments,0).toString();
            long result = parse(dateString, getEvaluator().getDefaultTimeZone());
            return result == -1L ? ESNumber.NaN : ESNumber.valueOf(result);
        }
    }

    private static class DateObjectUTC extends BuiltinFunctionObject {
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
    
    private static class DateObjectNow extends BuiltinFunctionObject {
        private static final long serialVersionUID = 6509035302938791509L;

        public DateObjectNow(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            return ESNumber.valueOf(System.currentTimeMillis());
        }
    }

    private static abstract class DateBuiltinFunctionObject extends BuiltinFunctionObject {
        private static final long serialVersionUID = 8694680238052474774L;

        DateBuiltinFunctionObject(FunctionPrototype fp, Evaluator evaluator,String name, int length) {
            super(fp, evaluator, name, 0);
        }
        
        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments) throws EcmaScriptException {
            if (!(thisObject instanceof DatePrototype)) {
                throw new TypeError("Function "+getFunctionName()+" must be called on a date");
            }
            DatePrototype aDate = (DatePrototype) thisObject;
            return callDateFunction(aDate,arguments);
        }
        
        protected abstract ESValue callDateFunction(DatePrototype aDate, ESValue[] arguments) throws EcmaScriptException;
    }
    // For datePrototype
    private static class DatePrototypeToString extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeToString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,
                ESValue[] arguments) throws EcmaScriptException {
            if (aDate.date == null) {
                return new ESString("NaN");
            }
            Evaluator evaluator = getEvaluator();
            DateFormat df = new SimpleDateFormat(TO_STRING_PATTERN);
            df.setTimeZone(evaluator.getDefaultTimeZone());
            return new ESString(df.format(aDate.date));
        }
    }

    private static class DatePrototypeToISOString extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeToISOString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate, ESValue[] arguments) throws EcmaScriptException {
           if (aDate.date == null) {
               throw new RangeError("Date is not valid");
           }
           DateFormat df = new SimpleDateFormat(SIMPLIFIED_ISO8601_DATE_FORMAT_PATTERN);
           df.setTimeZone(TimeZone.getTimeZone("UTC"));
           return new ESString(df.format(aDate.date));
        }

    }

    private static class DatePrototypeToJSON extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeToJSON(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject,
                ESValue[] arguments) throws EcmaScriptException {
            ESObject object = thisObject.toESObject(getEvaluator());
            if (!object.toESNumber().isFinite()) {
                return ESNull.theNull;
            }
            try {
                return object.doIndirectCall(getEvaluator(), object, "toISOString", EMPTY_ARRAY);
            } catch (NoSuchMethodException e) {
                throw new TypeError("toJSON: toISOString does not exists on object");
            }
        }
    }

    private static class DatePrototypeValueOf extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeValueOf(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate, ESValue[] arguments) throws EcmaScriptException {
            if (aDate.date == null) {
                return ESNumber.valueOf(Double.NaN);
            }
            long t = aDate.date.getTime();
            return ESNumber.valueOf(t);

        }
    }

    private static class DatePrototypeToLocaleString extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeToLocaleString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate, ESValue[] arguments) throws EcmaScriptException {
            DateFormat df = DateFormat.getDateTimeInstance();
            df.setTimeZone(TimeZone.getDefault());
            return (aDate.date == null) ? new ESString("NaN")
                    : new ESString(df.format(aDate.date));
        }
    }

    private static class DatePrototypeToGMTString extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeToGMTString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            DateFormat df = new SimpleDateFormat(UTC_FORMAT_PATTERN);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            return (aDate.date == null) ? new ESString("NaN")
                    : new ESString(df.format(aDate.date));
        }
    }

    private static class DatePrototypeGetYear extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetYear(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            ESValue v = aDate.get(Calendar.YEAR);
            return ESNumber.valueOf(v.doubleValue() - 1900);
        }
    }

    private static class DatePrototypeGetFullYear extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetFullYear(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.get(Calendar.YEAR);
        }
    }

    private static class DatePrototypeGetUTCFullYear extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetUTCFullYear(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.getUTC(Calendar.YEAR);
        }
    }

    private static class DatePrototypeGetMonth extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetMonth(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.get(Calendar.MONTH);
        }
    }

    private static class DatePrototypeGetUTCMonth extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetUTCMonth(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.getUTC(Calendar.MONTH);
        }
    }

    private static class DatePrototypeGetDate extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetDate(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.get(Calendar.DAY_OF_MONTH);
        }
    }

    private static class DatePrototypeGetUTCDate extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetUTCDate(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.getUTC(Calendar.DAY_OF_MONTH);
        }
    }

    private static class DatePrototypeGetDay extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetDay(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            // EcmaScript has SUNDAY=0, java SUNDAY=1 - converted in
            // DatePrototype
            return aDate.get(Calendar.DAY_OF_WEEK);
        }
    }

    private static class DatePrototypeGetUTCDay extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetUTCDay(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.getUTC(Calendar.DAY_OF_WEEK);
        }
    }

    private static class DatePrototypeGetHours extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetHours(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.get(Calendar.HOUR_OF_DAY);
        }
    }

    private static class DatePrototypeGetUTCHours extends DateBuiltinFunctionObject {

        private static final long serialVersionUID = 1L;

        DatePrototypeGetUTCHours(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.getUTC(Calendar.HOUR_OF_DAY);
        }
    }

    private static class DatePrototypeGetMinutes extends DateBuiltinFunctionObject {

        private static final long serialVersionUID = 1L;

        DatePrototypeGetMinutes(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.get(Calendar.MINUTE);
        }
    }

    private static class DatePrototypeGetUTCMinutes extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetUTCMinutes(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.getUTC(Calendar.MINUTE);
        }
    }

    private static class DatePrototypeGetSeconds extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetSeconds(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.get(Calendar.SECOND);
        }
    }

    private static class DatePrototypeGetUTCSeconds extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetUTCSeconds(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.getUTC(Calendar.SECOND);
        }
    }

    private static class DatePrototypeGetMilliseconds extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetMilliseconds(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.get(Calendar.MILLISECOND);
        }
    }

    private static class DatePrototypeGetUTCMilliseconds extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetUTCMilliseconds(String name,
                Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.getUTC(Calendar.MILLISECOND);
        }
    }

    private static class DatePrototypeSetYear extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetYear(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setYear(arguments);
        }
    }

    private static class DatePrototypeSetFullYear extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetFullYear(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setTime(arguments, new int[] { Calendar.YEAR,
                    Calendar.MONTH, Calendar.DAY_OF_MONTH });
        }
    }

    private static class DatePrototypeSetUTCFullYear extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetUTCFullYear(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setUTCTime(arguments, new int[] {
                    Calendar.YEAR, Calendar.MONTH,
                    Calendar.DAY_OF_MONTH });
        }
    }

    private static class DatePrototypeSetMonth extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetMonth(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setTime(arguments, new int[] { Calendar.MONTH,
                    Calendar.DAY_OF_MONTH });
        }
    }

    private static class DatePrototypeSetUTCMonth extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetUTCMonth(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setUTCTime(arguments, new int[] {
                    Calendar.MONTH, Calendar.DAY_OF_MONTH });
        }
    }

    private static class DatePrototypeSetDate extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetDate(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setTime(arguments,
                    new int[] { Calendar.DAY_OF_MONTH });
        }
    }

    private static class DatePrototypeSetUTCDate extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetUTCDate(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setUTCTime(arguments,
                    new int[] { Calendar.DAY_OF_MONTH });
        }
    }

    private static class DatePrototypeSetHours extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetHours(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setTime(arguments, new int[] {
                    Calendar.HOUR_OF_DAY, Calendar.MINUTE,
                    Calendar.SECOND, Calendar.MILLISECOND });
        }
    }

    private static class DatePrototypeSetUTCHours extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetUTCHours(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setUTCTime(arguments, new int[] {
                    Calendar.HOUR_OF_DAY, Calendar.MINUTE,
                    Calendar.SECOND, Calendar.MILLISECOND });
        }
    }

    private static class DatePrototypeSetMinutes extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetMinutes(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setTime(arguments, new int[] {
                    Calendar.MINUTE, Calendar.SECOND,
                    Calendar.MILLISECOND });
        }
    }

    private static class DatePrototypeSetUTCMinutes extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetUTCMinutes(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setUTCTime(arguments, new int[] {
                    Calendar.MINUTE, Calendar.SECOND,
                    Calendar.MILLISECOND });
        }
    }

    private static class DatePrototypeSetSeconds extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetSeconds(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setTime(arguments, new int[] {
                    Calendar.SECOND, Calendar.MILLISECOND });
        }
    }

    private static class DatePrototypeSetUTCSeconds extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetUTCSeconds(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setUTCTime(arguments, new int[] {
                    Calendar.SECOND, Calendar.MILLISECOND });
        }
    }

    private static class DatePrototypeSetMilliseconds extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetMilliseconds(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setTime(arguments,
                    new int[] { Calendar.MILLISECOND });
        }
    }

    private static class DatePrototypeSetUTCMilliseconds extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetUTCMilliseconds(String name,
                Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            return aDate.setUTCTime(arguments,
                    new int[] { Calendar.MILLISECOND });
        }
    }

    private static class DatePrototypeGetTimezoneOffset extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeGetTimezoneOffset(String name,
                Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
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

    private static class DatePrototypeSetTime extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeSetTime(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
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

    private static class DatePrototypeToDateString extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeToDateString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd yyyy");
            dateFormat.setTimeZone(getEvaluator().getDefaultTimeZone());
            return new ESString(dateFormat.format(aDate.date));
        }
    }

    private static class DatePrototypeToTimeString extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeToTimeString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss 'UTC'ZZZ");
            dateFormat.setTimeZone(getEvaluator().getDefaultTimeZone());
            return new ESString(dateFormat.format(aDate.date));
        }
    }

    private static class DatePrototypeToLocaleDateString extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeToLocaleDateString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, getEvaluator().getDefaultLocale());
            dateFormat.setTimeZone(getEvaluator().getDefaultTimeZone());
            return new ESString(dateFormat.format(aDate.date));
        }
    }

    private static class DatePrototypeToLocaleTimeString extends DateBuiltinFunctionObject {
        private static final long serialVersionUID = 1L;

        DatePrototypeToLocaleTimeString(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callDateFunction(DatePrototype aDate,ESValue[] arguments) throws EcmaScriptException {
            DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.LONG, getEvaluator().getDefaultLocale());
            dateFormat.setTimeZone(getEvaluator().getDefaultTimeZone());
            return new ESString(dateFormat.format(aDate.date));
        }
    }

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

        if (l == 0) {
            theObject.date = new Date();
        } else if (l == 1) {
            ESValue v = getArg(arguments,0).toESPrimitive();
            long time;
            if (v.getTypeOf() == EStypeString) {
                time = parse(v.toString(), getEvaluator().getDefaultTimeZone());
            } else {
                double d = arguments[0].doubleValue();
                time = (Double.isNaN(d)) ? -1 : (long) d;
            }
            theObject.date = (time == -1) ? null : new Date(time);
        } else {
            Date time = dateFromComponents(arguments,getEvaluator().getDefaultTimeZone());
            theObject.date = time;
        }
    }

    private Date dateFromComponents(ESValue[] arguments, TimeZone timeZone)
            throws EcmaScriptException {
        int [] dateComponents = new int[7];
        for( int i=0; i<7; i++) {
            if (i >= arguments.length) {
                if (i<=MONTH_POSITION) {
                    return null;
                }
                if (i == DAY_POSITION) {
                    dateComponents[i] = 1;
                } else {
                    dateComponents[i] = 0;
                }
            } else {
                double v = arguments[i].doubleValue();
                if (Double.isNaN(v) || Double.isInfinite(v)) {
                    return null;
                }
                dateComponents[i] = (int)v;
            }
        }
        int year = dateComponents[YEAR_POSITION];
        if (0 <= year && year <= 99) {
            year += 1900;
        }
        int month = dateComponents[MONTH_POSITION];
        int day = dateComponents[DAY_POSITION];
        int hour = dateComponents[HOUR_POSITION];
        int minute = dateComponents[MINUTE_POSITION];
        int second = dateComponents[SECOND_POSITION];
        int ms = dateComponents[MILLISECOND_POSITION];
        // Using current current locale, set it to the specified time
        // System.out.println("YEAR IS " + year);
        Date time = mktime(timeZone, year, month, day, hour, minute, second, ms);
        if (time.before(MIN_DATE) || time.after(MAX_DATE)) {
            return null;
        }
        return time;
    }

    private static final Date MAX_DATE=mktime(TimeZone.getTimeZone("UTC"),1970,0,100000001,0,0,0,0);
    private static final Date MIN_DATE=mktime(TimeZone.getTimeZone("UTC"),1970,0,-99999999,0,0,0,1);
    
    private static Date mktime(TimeZone timeZone, int year, int month, int day,
            int hour, int minute, int second, int ms) {
        GregorianCalendar cal = new GregorianCalendar(timeZone);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, ms);
        return cal.getTime();
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

 
            // For dateObject
            dateObject.putProperty(StandardProperty.PROTOTYPEstring, 0, datePrototype);
            dateObject.putHiddenProperty("length", ESNumber.valueOf(7));
            dateObject.putHiddenProperty("parse", new DateObjectParse("parse",
                    evaluator, functionPrototype));
            dateObject.putHiddenProperty("UTC", new DateObjectUTC("UTC",
                    evaluator, functionPrototype));
            dateObject.putHiddenProperty("now", new DateObjectNow("now",
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
            datePrototype.putHiddenProperty("toISOString",
                    new DatePrototypeToISOString("toISOString", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("toJSON",
                    new DatePrototypeToJSON("toJSON", evaluator,
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
            datePrototype.putHiddenProperty("toDateString",
                    new DatePrototypeToDateString("toDateString", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("toTimeString",
                    new DatePrototypeToTimeString("toTimeString", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("toLocaleDateString",
                    new DatePrototypeToLocaleDateString("toLocaleDateString", evaluator,
                            functionPrototype));
            datePrototype.putHiddenProperty("toLocaleTimeString",
                    new DatePrototypeToLocaleTimeString("toLocaleTimeString", evaluator,
                            functionPrototype));

        } catch (EcmaScriptException e) {
            e.printStackTrace();
            throw new ProgrammingError(e.getMessage());
        }
    }

    private static abstract class DateParser {
        public abstract Date parse(String dateString, TimeZone timeZone);
    }
    private static abstract class DateFormatParser extends DateParser {
        
        @Override
        public Date parse(String dateString, TimeZone timeZone) {
            DateFormat formatter = getFormatter(timeZone);
            ParsePosition initialPosition = new ParsePosition(0);
            return formatter.parse(dateString,initialPosition);
        }

        protected abstract DateFormat getFormatter(TimeZone timeZone);
    }
    
    private static class UTCDateParser extends DateFormatParser {
        private final String pattern;
        private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

        public UTCDateParser(String  pattern) {
            this.pattern = pattern;
        }
        
        @Override
        public DateFormat getFormatter(TimeZone timeZone) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            df.setTimeZone(UTC_TIME_ZONE);
            return df;
        }
    }
    
    private static class TZDateParser extends DateFormatParser {
        private final String pattern;

        public TZDateParser(String  pattern) {
            this.pattern = pattern;
        }
        
        @Override
        public DateFormat getFormatter(TimeZone timeZone) {
            SimpleDateFormat df = new SimpleDateFormat(pattern);
            df.setTimeZone(timeZone);
            return df;
        }
    }
    
    private static class DefaultDateParser extends DateFormatParser {

        @Override
        protected DateFormat getFormatter(TimeZone timeZone) {
            DateFormat df = DateFormat.getDateTimeInstance();
            df.setTimeZone(timeZone);
            return df;
        }
        
    }


    private static DateParser [] parsers = {
            new UTCDateParser(SIMPLIFIED_ISO8601_DATE_FORMAT_PATTERN),
            new UTCDateParser(UTC_FORMAT_PATTERN),
            new TZDateParser(TO_STRING_PATTERN), 
            new DefaultDateParser(),
            new UTCDateParser("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            new UTCDateParser("yyyy-MM-dd'T'HH:mm:ss"),
            new UTCDateParser("yyyy-MM-dd'T'HH:mm"),
            new UTCDateParser("yyyy-MM-dd'T'HH"),
            new UTCDateParser("yyyy-MM-dd"),
            new UTCDateParser("yyyy-MM"),
            new UTCDateParser("yyyy"),
            };
    private static long parse(String dateString, TimeZone timeZone) {
        for (DateParser parser : parsers) {
            Date date = parser.parse(dateString,timeZone);
            if (date != null) {
                return date.getTime();
            }
        }
        return -1L;
    }
}
