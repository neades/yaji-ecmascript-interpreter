// ORORegExp.java
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

package FESI.Extensions;

import java.util.regex.*;

import FESI.Data.ArrayPrototype;
import FESI.Data.BuiltinFunctionObject;
import FESI.Data.ESBoolean;
import FESI.Data.ESNull;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESPrimitive;
import FESI.Data.ESString;
import FESI.Data.ESValue;
import FESI.Data.FunctionPrototype;
import FESI.Data.GlobalObject;
import FESI.Data.ObjectPrototype;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ScopeChain;

/**
 * An EcmaScript RegExp object based on OROInc pattern matcher. May not coexist
 * with the GNU regexp matcher.
 */
class ESJavaRegExp extends ESObject {

    private static final long serialVersionUID = 1319585947413414602L;
    private String regExpString;
    private boolean ignoreCase = false;
    private boolean global = false;
    private Pattern pattern = null; // null means no valid pattern

    static private final String IGNORECASEstring = ("ignoreCase").intern();
    static private final int IGNORECASEhash = IGNORECASEstring.hashCode();
    static private final String GLOBALstring = ("global").intern();
    static private final int GLOBALhash = GLOBALstring.hashCode();

    // Normal constructor
    ESJavaRegExp(ESObject prototype, Evaluator evaluator, String regExpString) {
        super(prototype, evaluator);
        this.regExpString = regExpString;
    }

    // Prototype constructor
    ESJavaRegExp(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
        this.regExpString = "";
    }

    public Pattern getPattern() throws EcmaScriptException {

        if (pattern == null) {
            compile();
        }
        return pattern;
    }

    public boolean isGlobal() {
        return global;
    }

    public void compile() throws EcmaScriptException {
        // Recompile the pattern
        try {
            pattern = Pattern.compile(regExpString,
                    ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
        } catch (PatternSyntaxException e) {
            throw new EcmaScriptException("PatternSyntaxException: /"
                    + regExpString + "/", e);
        }
    }

    public String getESClassName() {
        return "RegExp";
    }

    public String toString() {
        if (regExpString == null)
            return "/<null>/";
        return "/" + regExpString + "/";
    }

    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + this.toString() + "]";
    }

    public ESValue getPropertyInScope(String propertyName,
            ScopeChain previousScope, int hash) throws EcmaScriptException {
        if (propertyName.equals(IGNORECASEstring)) {
            return ESBoolean.valueOf(ignoreCase);
        } else if (propertyName.equals(GLOBALstring)) {
            return ESBoolean.valueOf(global);
        }
        return super.getPropertyInScope(propertyName, previousScope, hash);
    }

    public ESValue getProperty(String propertyName, int hash)
            throws EcmaScriptException {
        if (propertyName.equals(IGNORECASEstring)) {
            return ESBoolean.valueOf(ignoreCase);
        } else if (propertyName.equals(GLOBALstring)) {
            return ESBoolean.valueOf(global);
        } else {
            return super.getProperty(propertyName, hash);
        }
    }

    public void putProperty(String propertyName, ESValue propertyValue, int hash)
            throws EcmaScriptException {
        if (hash == IGNORECASEhash && propertyName.equals(IGNORECASEstring)) {
            boolean oldIgnoreCase = ignoreCase;
            ignoreCase = (((ESPrimitive) propertyValue).booleanValue());
            if (oldIgnoreCase != ignoreCase)
                pattern = null; // force recompilation
        } else if (hash == GLOBALhash && propertyName.equals(GLOBALstring)) {
            global = (((ESPrimitive) propertyValue).booleanValue());
        } else {
            super.putProperty(propertyName, propertyValue, hash);
        }
    }

    public String[] getSpecialPropertyNames() {
        String[] ns = { GLOBALstring, IGNORECASEstring };
        return ns;
    }
}

public class JavaRegExp extends Extension {

    private static final long serialVersionUID = 7106811224527635297L;
    static private final String INDEXstring = ("index").intern();
    static private final int INDEXhash = INDEXstring.hashCode();
    static private final String INPUTstring = ("input").intern();
    static private final int INPUThash = INPUTstring.hashCode();

    private ESObject esRegExpPrototype;

    static class ESRegExpPrototypeTest extends BuiltinFunctionObject {
        private static final long serialVersionUID = -1530678844987141170L;

        ESRegExpPrototypeTest(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            if (arguments.length < 1) {
                throw new EcmaScriptException("test requires 1 string argument");
            }
            ESJavaRegExp pattern = (ESJavaRegExp) thisObject;
            String str = arguments[0].toString();
            Matcher matcher = pattern.getPattern().matcher(str);
            return ESBoolean.valueOf(matcher.find());
        }
    }

    static class ESRegExpPrototypeExec extends BuiltinFunctionObject {
        private static final long serialVersionUID = 6552738494467189408L;

        ESRegExpPrototypeExec(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            if (arguments.length < 1) {
                throw new EcmaScriptException("exec requires 1 string argument");
            }
            ESJavaRegExp pattern = (ESJavaRegExp) thisObject;
            String str = arguments[0].toString();
            Matcher matcher = pattern.getPattern().matcher(str);
            boolean result = matcher.find();
            if (result) {
                // at least one match
                ESObject ap = this.getEvaluator().getArrayPrototype();
                ArrayPrototype resultArray = new ArrayPrototype(ap, this
                        .getEvaluator());
                resultArray.putProperty(INDEXstring, ESNumber.valueOf(matcher
                        .start()), INDEXhash);
                resultArray.putProperty(INPUTstring, new ESString(str),
                        INPUThash);
                resultArray.setSize(matcher.groupCount() + 1);
                for (int i = 0; i <= matcher.groupCount(); i++) {
                    resultArray.setElementAt(new ESString(matcher.group(i)), i);
                } // for
                return resultArray;
            } else {
                return ESNull.theNull;
            }
        }
    }

    class GlobalObjectRegExp extends BuiltinFunctionObject {
        private static final long serialVersionUID = 5695969400216749530L;

        GlobalObjectRegExp(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            return doConstruct(thisObject.toESObject(getEvaluator()), arguments);
        }

        public ESObject doConstruct(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {

            ESJavaRegExp regExp = null;
            if (arguments.length == 0) {
                throw new EcmaScriptException(
                        "RegExp requires 1 or 2 arguments");
            } else if (arguments.length == 1) {
                regExp = new ESJavaRegExp(esRegExpPrototype, this
                        .getEvaluator(), arguments[0].toString());
            }
            return regExp;
        }
    }

    public JavaRegExp() {
        super();
    }

    public void initializeExtension(Evaluator evaluator)
            throws EcmaScriptException {

        GlobalObject go = evaluator.getGlobalObject();
        ObjectPrototype op = (ObjectPrototype) evaluator.getObjectPrototype();
        FunctionPrototype fp = (FunctionPrototype) evaluator
                .getFunctionPrototype();
        esRegExpPrototype = new ESJavaRegExp(op, evaluator);

        ESObject globalObjectRegExp = new GlobalObjectRegExp("RegExp",
                evaluator, fp);

        globalObjectRegExp.putHiddenProperty("prototype", esRegExpPrototype);
        globalObjectRegExp.putHiddenProperty("length", ESNumber.valueOf(1));

        esRegExpPrototype.putHiddenProperty("constructor", globalObjectRegExp);
        esRegExpPrototype.putHiddenProperty("test", new ESRegExpPrototypeTest(
                "test", evaluator, fp));
        esRegExpPrototype.putHiddenProperty("exec", new ESRegExpPrototypeExec(
                "exec", evaluator, fp));

        go.putHiddenProperty("RegExp", globalObjectRegExp);

        class StringPrototypeSearch extends BuiltinFunctionObject {
            private static final long serialVersionUID = -3110437308530985673L;

            StringPrototypeSearch(String name, Evaluator evaluator,
                    FunctionPrototype fp) {
                super(fp, evaluator, name, 1);
            }

            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                if (arguments.length < 1) {
                    throw new EcmaScriptException(
                            "search requires 1 pattern argument");
                }
                String str = thisObject.toString();
                ESJavaRegExp pattern;
                if (arguments[0] instanceof ESJavaRegExp) {
                    pattern = (ESJavaRegExp) arguments[0];
                } else {
                    throw new EcmaScriptException(
                            "The search argument must be a RegExp");
                }
                Matcher matcher = pattern.getPattern().matcher(str);
                if (matcher.find()) {
                    return ESNumber.valueOf(matcher.start());
                } else {
                    return ESNumber.valueOf(-1);
                }
            }
        }

        class StringPrototypeReplace extends BuiltinFunctionObject {
            private static final long serialVersionUID = -5972680989663620329L;

            StringPrototypeReplace(String name, Evaluator evaluator,
                    FunctionPrototype fp) {
                super(fp, evaluator, name, 1);
            }

            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                if (arguments.length < 2) {
                    throw new EcmaScriptException(
                            "replace requires 2 arguments: pattern and replacement string");
                }
                String str = thisObject.toString();
                ESJavaRegExp pattern;
                if (arguments[0] instanceof ESJavaRegExp) {
                    pattern = (ESJavaRegExp) arguments[0];
                } else {
                    throw new EcmaScriptException(
                            "The replace argument must be a RegExp");
                }
                Matcher matcher = pattern.getPattern().matcher(str);
                String replacement = arguments[1].toString();

                if (pattern.isGlobal()) {
                    return new ESString(matcher.replaceAll(replacement));
                } else {
                    return new ESString(matcher.replaceFirst(replacement));
                }
            }
        }

        class StringPrototypeMatch extends BuiltinFunctionObject {
            private static final long serialVersionUID = -4817060573308387732L;

            StringPrototypeMatch(String name, Evaluator evaluator,
                    FunctionPrototype fp) {
                super(fp, evaluator, name, 1);
            }

            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                if (arguments.length < 1) {
                    throw new EcmaScriptException(
                            "match requires 1 pattern argument");
                }
                String str = thisObject.toString();
                ESJavaRegExp pattern;
                if (arguments[0] instanceof ESJavaRegExp) {
                    pattern = (ESJavaRegExp) arguments[0];
                } else {
                    throw new EcmaScriptException(
                            "The match argument must be a RegExp");
                }
                Matcher matcher = pattern.getPattern().matcher(str);

                boolean result = matcher.find();
                if (result) {
                    // at least one match
                    ESObject ap = this.getEvaluator().getArrayPrototype();
                    ArrayPrototype resultArray = new ArrayPrototype(ap, this
                            .getEvaluator());
                    resultArray.putProperty(INDEXstring, ESNumber
                            .valueOf(matcher.start()), INDEXhash);
                    resultArray.putProperty(INPUTstring, new ESString(str),
                            INPUThash);
                    resultArray.setSize(matcher.groupCount() + 1);
                    for (int i = 0; i <= matcher.groupCount(); i++) {
                        resultArray.setElementAt(
                                new ESString(matcher.group(i)), i);
                    } // for
                    return resultArray;
                } else {
                    return ESNull.theNull;
                }
            }
        }

        class StringPrototypeSplit extends BuiltinFunctionObject {
            private static final long serialVersionUID = 2991853591081656659L;

            StringPrototypeSplit(String name, Evaluator evaluator,
                    FunctionPrototype fp) {
                super(fp, evaluator, name, 1);
            }

            public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                    throws EcmaScriptException {
                String str = thisObject.toString();
                ESObject ap = this.getEvaluator().getArrayPrototype();
                ArrayPrototype theArray = new ArrayPrototype(ap, this
                        .getEvaluator());
                if (arguments.length <= 0) {
                    theArray.setSize(1);
                    theArray.setElementAt(thisObject, 0);
                } else {
                    if (arguments[0] instanceof ESJavaRegExp) {
                        ESJavaRegExp pattern = (ESJavaRegExp) arguments[0];
                        Pattern spliter = pattern.getPattern();
                        String[] result = null;
                        if (arguments.length > 1) {
                            int n = arguments[1].toUInt32();
                            result = spliter.split(str, n);
                        } else {
                            result = spliter.split(str);
                        }
                        int l = result.length;
                        theArray.setSize(l);
                        for (int i = 0; i < l; i++) {
                            theArray.setElementAt(new ESString(result[i]), i);
                        }

                    } else { // ! instanceof ESJavaRegExp, using "normal" split
                        String sep = arguments[0].toString();
                        if (sep.length() == 0) {
                            int l = str.length();
                            theArray.setSize(l);
                            for (int i = 0; i < l; i++) {
                                theArray.setElementAt(new ESString(str
                                        .substring(i, i + 1)), i);
                            }
                        } else {
                            int i = 0;
                            int start = 0;
                            while (start < str.length()) {
                                int pos = str.indexOf(sep, start);
                                if (pos < 0)
                                    pos = str.length();
                                // System.out.println("start: " + start +
                                // ", pos: " + pos);
                                theArray.setSize(i + 1);
                                theArray.setElementAt(new ESString(str
                                        .substring(start, pos)), i);
                                start = pos + sep.length();
                                i++;
                            }
                        }
                    } // instanceof ESORORegExp
                }
                return theArray;
            }
        }

        ESObject stringPrototype = evaluator.getStringPrototype();
        stringPrototype.putHiddenProperty("search", new StringPrototypeSearch(
                "search", evaluator, fp));
        stringPrototype.putHiddenProperty("replace",
                new StringPrototypeReplace("replace", evaluator, fp));
        stringPrototype.putHiddenProperty("match", new StringPrototypeMatch(
                "match", evaluator, fp));
        stringPrototype.putHiddenProperty("split", new StringPrototypeSplit(
                "split", evaluator, fp));

        OptionalRegExp.setLoadedRegExp(this);

    }
}