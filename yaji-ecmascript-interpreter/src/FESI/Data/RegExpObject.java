package FESI.Data;

import java.util.regex.Pattern;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.SyntaxError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

class RegExpObject extends BuiltinFunctionObject {
    private static final long serialVersionUID = 5695969400216749530L;
    private final ESObject regExpPrototype;
    
    private static class ESRegExpPrototypetestMethod extends BuiltinFunctionObject {
        private static final long serialVersionUID = -1530678844987141170L;

        ESRegExpPrototypetestMethod(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESValue string = (arguments.length < 1)?ESUndefined.theUndefined:arguments[0];
            RegExpPrototype pattern = (RegExpPrototype) thisObject;
            return ESBoolean.valueOf(pattern.exec(string) != ESNull.theNull);
        }
    }

    private static class ESRegExpPrototypeExecMethod extends BuiltinFunctionObject {
        private static final long serialVersionUID = 6552738494467189408L;

        ESRegExpPrototypeExecMethod(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESValue string = (arguments.length < 1)?ESUndefined.theUndefined:arguments[0];
            RegExpPrototype pattern = (RegExpPrototype) thisObject;
            return pattern.exec(string);
        }
    }

    private static class ESRegExpPrototypeToStringMethod extends BuiltinFunctionObject {
        private static final long serialVersionUID = 6552738494467189408L;

        ESRegExpPrototypeToStringMethod(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            if (!(thisObject instanceof RegExpPrototype)) {
                throw new TypeError("Regexp.prototype.toString cannot be applied to non-regexp object");
            }
            RegExpPrototype regexp = (RegExpPrototype) thisObject;
            return new ESString(regexp.toString());
        }
    }

    public RegExpObject(String name, Evaluator evaluator,
            FunctionPrototype fp, ESObject regExpPrototype) throws EcmaScriptException {
        super(fp, evaluator, name, 1);
        this.regExpPrototype = regExpPrototype;
        regExpPrototype.putHiddenProperty("test", new ESRegExpPrototypetestMethod(
                "test", evaluator, fp));
        regExpPrototype.putHiddenProperty("exec", new ESRegExpPrototypeExecMethod(
                "exec", evaluator, fp));
        regExpPrototype.putHiddenProperty("toString", new ESRegExpPrototypeToStringMethod(
                "toString", evaluator, fp));
    }

    @Override
    public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
            throws EcmaScriptException {
        ESValue arg0 = getArg(arguments,0);
        ESValue arg1 = getArg(arguments,1);
        if (arg0 instanceof RegExpPrototype && arg1 == ESUndefined.theUndefined) {
            return arg0;
        }
        return doConstruct(arg0,arg1);
    }

    @Override
    public ESObject doConstruct(ESValue[] arguments)
            throws EcmaScriptException {
        return doConstruct(getArg(arguments, 0), getArg(arguments, 1));
    }

    private ESObject doConstruct(ESValue arg0, ESValue arg1) throws TypeError,
            SyntaxError {
        RegExpPrototype regExp = null;
        if (arg0 instanceof RegExpPrototype) {
            if (arg1 != ESUndefined.theUndefined) {
                throw new TypeError("RegExp(<regexp>) only takes one parameter");
            }
            regExp = new RegExpPrototype(regExpPrototype, getEvaluator(), (RegExpPrototype)arg0);
        } else {
            regExp = new RegExpPrototype(regExpPrototype, getEvaluator(), argToString(arg0), validateFlags(argToString(arg1)));
        }
        return regExp;
    }
    
    private static Pattern flagValidator = Pattern.compile("(g(?![^g]*g)|i(?![^i]*i)|m(?![^m]*m)){0,3}");
    private String validateFlags(String flags) throws SyntaxError {
        if (!flagValidator.matcher(flags).matches()) {
            throw new SyntaxError("Invalid flags "+flags+" - expect g,i or m");
        }
        return flags;
    }

    private String argToString(ESValue v) {
        return (v == ESUndefined.theUndefined)?"":v.toString();
    }
    

}