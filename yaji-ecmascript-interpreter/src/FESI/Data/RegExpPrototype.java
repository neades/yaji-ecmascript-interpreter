package FESI.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ScopeChain;

public class RegExpPrototype extends ESObject {

    private static final long serialVersionUID = 1319585947413414602L;
    private String regExpString;
    private boolean ignoreCase = false;
    private boolean global = false;
    private boolean multiline = false;
    private int lastIndex = 0;
    private Pattern pattern = null; // null means no valid pattern

    // Prototype constructor
    public RegExpPrototype(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
        this.regExpString = "";
    }

    public RegExpPrototype(ESObject regExpPrototype, Evaluator evaluator,
            String body, String flags) {
        super(regExpPrototype,evaluator);
        this.regExpString = body;
        global = flags.contains("g");
        ignoreCase = flags.contains("i");
        multiline = flags.contains("m");
    }

    public RegExpPrototype(ESObject regExpPrototype, Evaluator evaluator, RegExpPrototype other) {
        super(regExpPrototype,evaluator);
        this.regExpString = other.regExpString;
        this.global = other.global;
        this.ignoreCase = other.ignoreCase;
        this.multiline = other.multiline;
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
                    (ignoreCase ? Pattern.CASE_INSENSITIVE : 0)
                    | (multiline ? Pattern.MULTILINE : 0));
        } catch (PatternSyntaxException e) {
            throw new EcmaScriptException("PatternSyntaxException: /"
                    + regExpString + "/", e);
        }
    }

    @Override
    public String getESClassName() {
        return "RegExp";
    }

    @Override
    public String toString() {
        if (regExpString == null)
            return "/<null>/";
        return "/" + regExpString + "/" 
                + (global?"g":"")
                + (ignoreCase?"i":"")
                + (multiline?"m":"");
    }

    @Override
    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + this.toString() + "]";
    }

    @Override
    public ESValue getPropertyInScope(String propertyName,
            ScopeChain previousScope, int hash) throws EcmaScriptException {
        ESValue result = getLocalProperty(propertyName, hash);
        return (result == null) ? super.getPropertyInScope(propertyName, previousScope, hash) : result;
    }

    @Override
    public ESValue getPropertyIfAvailable(String propertyName, int hash)
            throws EcmaScriptException {
        ESValue result = getLocalProperty(propertyName, hash);
        return (result == null) ? super.getPropertyIfAvailable(propertyName, hash) : result;
    }
    
    private ESValue getLocalProperty(String propertyName, int hash) {
        if (hash == StandardProperty.IGNORE_CASEhash && propertyName.equals(StandardProperty.IGNORE_CASEstring)) {
            return ESBoolean.valueOf(ignoreCase);
        } else if (hash == StandardProperty.GLOBALhash && propertyName.equals(StandardProperty.GLOBALstring)) {
            return ESBoolean.valueOf(global);
        } else if (hash == StandardProperty.MULTILINEhash && propertyName.equals(StandardProperty.MULTILINEstring)) {
            return ESBoolean.valueOf(multiline);
        } else if (hash == StandardProperty.LAST_INDEXhash && propertyName.equals(StandardProperty.LAST_INDEXstring)) {
            return ESNumber.valueOf(lastIndex);
        } else if (hash == StandardProperty.SOURCEhash && propertyName.equals(StandardProperty.SOURCEstring)) {
            return new ESString(regExpString);
        }
        return null;
    }

    @Override
    public void putProperty(String propertyName, ESValue propertyValue, int hash)
            throws EcmaScriptException {
        if ((hash == StandardProperty.IGNORE_CASEhash && propertyName.equals(StandardProperty.IGNORE_CASEstring))
            || (hash == StandardProperty.MULTILINEhash && propertyName.equals(StandardProperty.MULTILINEstring))
            || (hash == StandardProperty.GLOBALhash && propertyName.equals(StandardProperty.GLOBALstring))
            || (hash == StandardProperty.SOURCEhash && propertyName.equals(StandardProperty.SOURCEstring))) {
            // not writable
        } else if (hash == StandardProperty.LAST_INDEXhash && propertyName.equals(StandardProperty.LAST_INDEXstring)) {
            lastIndex = propertyValue.toInt32();
        } else {
            super.putProperty(propertyName, propertyValue, hash);
        }
    }

    @Override
    public String[] getSpecialPropertyNames() {
        String[] ns = { StandardProperty.GLOBALstring, StandardProperty.IGNORE_CASEstring, StandardProperty.MULTILINEstring, StandardProperty.LAST_INDEXstring, StandardProperty.SOURCEstring };
        return ns;
    }

    ESValue exec(ESValue value) throws EcmaScriptException {
        String str = value.toString();
        String string;
        int startIndex = 0;
        if (global) {
            if (lastIndex >= 0 && lastIndex <= str.length()) {
                string = str.substring(lastIndex);
                startIndex = lastIndex;
            } else {
                lastIndex = 0;
                return ESNull.theNull;
            }
        } else {
            string = str;
        }
        Matcher matcher = getPattern().matcher(string);
        boolean result = matcher.find();
        if (result) {
            // at least one match
            ESObject resultArray = getEvaluator().createArray();
            resultArray.putProperty(StandardProperty.INDEXstring, ESNumber.valueOf(matcher.start()), StandardProperty.INDEXhash);
            resultArray.putProperty(StandardProperty.INPUTstring, new ESString(str),StandardProperty.INPUThash);
            for (int i = 0; i <= matcher.groupCount(); i++) {
                resultArray.putProperty((long)i,new ESString(matcher.group(i)));
            } // for
            lastIndex = matcher.end() + startIndex;
            return resultArray;
        }
        lastIndex = 0;
        return ESNull.theNull;
    }

}