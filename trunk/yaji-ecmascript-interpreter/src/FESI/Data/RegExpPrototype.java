package FESI.Data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;

public class RegExpPrototype extends ESObject {

    private static final long serialVersionUID = 1319585947413414602L;
    private Pattern pattern = null; // null means no valid pattern

    // Prototype constructor
    public RegExpPrototype(ESObject prototype, Evaluator evaluator) throws EcmaScriptException {
        super(prototype,evaluator);
        putProperty(StandardProperty.SOURCEstring, 0, new ESString(""));
        putProperty(StandardProperty.GLOBALstring, 0, ESBoolean.valueOf(false));
        putProperty(StandardProperty.IGNORE_CASEstring, 0, ESBoolean.valueOf(false));
        putProperty(StandardProperty.MULTILINEstring, 0, ESBoolean.valueOf(false));
        putProperty(StandardProperty.LAST_INDEXstring, WRITEABLE, ESNumber.valueOf(0));
    }

    public RegExpPrototype(ESObject regExpPrototype, Evaluator evaluator,
            String body, String flags) throws EcmaScriptException {
        this(regExpPrototype, evaluator, body, flags.contains("i"), flags.contains("g"), flags.contains("m"));
    }

    public RegExpPrototype(ESObject regExpPrototype, Evaluator evaluator, RegExpPrototype other) throws EcmaScriptException {
        this(regExpPrototype, evaluator, other.getSource(), other.isIgnoreCase(), other.isGlobal(), other.isMultiline());
    }
    
    private RegExpPrototype(ESObject regExpPrototype, Evaluator evaluator, String source, boolean ignoreCase, boolean global, boolean multiline) throws EcmaScriptException {
        super(regExpPrototype,evaluator);
        putProperty(StandardProperty.SOURCEstring,0,new ESString(source));
        putProperty(StandardProperty.GLOBALstring,0,ESBoolean.valueOf(global));
        putProperty(StandardProperty.IGNORE_CASEstring,0,ESBoolean.valueOf(ignoreCase));
        putProperty(StandardProperty.MULTILINEstring,0,ESBoolean.valueOf(multiline));
        putProperty(StandardProperty.LAST_INDEXstring, WRITEABLE, ESUndefined.theUndefined);
    }

    public Pattern getPattern() throws EcmaScriptException {

        if (pattern == null) {
            compile();
        }
        return pattern;
    }

    public void compile() throws EcmaScriptException {
        // Recompile the pattern
        try {
            pattern = Pattern.compile(getSource(),
                    (isIgnoreCase() ? Pattern.CASE_INSENSITIVE : 0)
                    | (isMultiline() ? Pattern.MULTILINE : 0));
        } catch (PatternSyntaxException e) {
            throw new EcmaScriptException("PatternSyntaxException: /"
                    + getSource() + "/", e);
        }
    }

    @Override
    public String getESClassName() {
        return "RegExp";
    }

    @Override
    public String callToString() throws EcmaScriptException {
        String source = getSource();
        if (source == null)
            return "/<null>/";
        return "/" + source + "/" 
                + (isGlobal()?"g":"")
                + (isIgnoreCase()?"i":"")
                + (isMultiline()?"m":"");
    }

    @Override
    public String toString() {
        try {
            return callToString();
        } catch( EcmaScriptException e ) {
            return "*RegExp - toString Exception - "+e.getMessage();
        }
    }
    
    @Override
    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + this.toString() + "]";
    }

    @Override
    public String[] getSpecialPropertyNames() {
        String[] ns = { StandardProperty.GLOBALstring, StandardProperty.IGNORE_CASEstring, StandardProperty.MULTILINEstring, StandardProperty.LAST_INDEXstring, StandardProperty.SOURCEstring };
        return ns;
    }

    ESValue exec(ESValue value) throws EcmaScriptException {
        String str = value.callToString();
        String string;
        int startIndex = 0;
        if (isGlobal()) {
            int lastIndex = getLastIndex();
            if (lastIndex >= 0 && lastIndex <= str.length()) {
                string = str.substring(lastIndex);
                startIndex = lastIndex;
            } else {
                setLastIndex(0);
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
                String group = matcher.group(i);
                ESValue groupValue = group != null ? new ESString(group) : ESUndefined.theUndefined;
                resultArray.putProperty((long)i,groupValue);
            } // for
            setLastIndex(matcher.end() + startIndex);
            return resultArray;
        }
        setLastIndex(0);
        return ESNull.theNull;
    }

    private boolean isMultiline() throws EcmaScriptException {
        return getProperty(StandardProperty.MULTILINEstring,StandardProperty.MULTILINEhash).booleanValue();
    }

    public boolean isGlobal() throws EcmaScriptException {
        return getProperty(StandardProperty.GLOBALstring,StandardProperty.GLOBALhash).booleanValue();
    }

    private boolean isIgnoreCase() throws EcmaScriptException {
        return getProperty(StandardProperty.IGNORE_CASEstring,StandardProperty.IGNORE_CASEhash).booleanValue();
    }

    private String getSource() throws EcmaScriptException {
        return getProperty(StandardProperty.SOURCEstring,StandardProperty.SOURCEhash).callToString();
    }

    public int getLastIndex() throws EcmaScriptException {
        return getProperty(StandardProperty.LAST_INDEXstring,StandardProperty.LAST_INDEXhash).toInt32();
    }

    void setLastIndex(int lastIndex) throws EcmaScriptException {
        putProperty(StandardProperty.LAST_INDEXstring,ESNumber.valueOf(lastIndex),StandardProperty.LAST_INDEXhash);
    }

}