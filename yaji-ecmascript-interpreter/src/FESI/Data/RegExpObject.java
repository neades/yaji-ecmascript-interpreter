package FESI.Data;

import java.util.regex.Pattern;

import FESI.Exceptions.EcmaScriptException;
import FESI.Exceptions.SyntaxError;
import FESI.Exceptions.TypeError;
import FESI.Interpreter.Evaluator;

class RegExpObject extends BuiltinFunctionObject {
    private static final long serialVersionUID = 5695969400216749530L;
    private final ESObject regExpPrototype;
    
    private static abstract class RegExpBuiltInFunctionObject extends BuiltinFunctionObject {
        private static final long serialVersionUID = 7234287006807912962L;

        RegExpBuiltInFunctionObject(FunctionPrototype fp, Evaluator evaluator,
                String name, int length) throws EcmaScriptException {
            super(fp, evaluator, name, length);
        }
        
        @Override
        public ESValue callFunction(ESValue thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            if (thisObject instanceof RegExpPrototype) {
                return callFunction((RegExpPrototype)thisObject, arguments);
            }
            throw new TypeError("RegExp.prototype."+getFunctionName()+" can only be applied to a RegExp instance");
        }
        
        protected abstract ESValue callFunction(RegExpPrototype thisRegexp, ESValue[] arguments) throws EcmaScriptException;
    }
    
    private static class ESRegExpPrototypetestMethod extends ESRegExpPrototypeExecMethod {
        private static final long serialVersionUID = -1530678844987141170L;

        ESRegExpPrototypetestMethod(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(name, evaluator, fp);
        }

        @Override
        public ESValue callFunction(RegExpPrototype pattern, ESValue[] arguments)
                throws EcmaScriptException {
            return ESBoolean.valueOf(super.callFunction(pattern, arguments) != ESNull.theNull);
        }
    }

    private static class ESRegExpPrototypeExecMethod extends RegExpBuiltInFunctionObject {
        private static final long serialVersionUID = 6552738494467189408L;

        ESRegExpPrototypeExecMethod(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 1);
        }

        @Override
        public ESValue callFunction(RegExpPrototype pattern, ESValue[] arguments)
                throws EcmaScriptException {
            ESValue string = (arguments.length < 1)?ESUndefined.theUndefined:arguments[0];
            return pattern.exec(string);
        }
    }

    private static class ESRegExpPrototypeToStringMethod extends RegExpBuiltInFunctionObject {
        private static final long serialVersionUID = 6552738494467189408L;

        ESRegExpPrototypeToStringMethod(String name, Evaluator evaluator,
                FunctionPrototype fp) throws EcmaScriptException {
            super(fp, evaluator, name, 0);
        }

        @Override
        public ESValue callFunction(RegExpPrototype regexp, ESValue[] arguments) {
            return new ESString(regexp.toString());
        }
    }

    public RegExpObject(String name, Evaluator evaluator,
            FunctionPrototype fp, ESObject regExpPrototype) throws EcmaScriptException {
        super(fp, evaluator, name, 2);
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

    private ESObject doConstruct(ESValue arg0, ESValue arg1) throws EcmaScriptException {
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

    private String argToString(ESValue v) throws EcmaScriptException {
        return (v == ESUndefined.theUndefined)?"":v.callToString();
    }
    

}