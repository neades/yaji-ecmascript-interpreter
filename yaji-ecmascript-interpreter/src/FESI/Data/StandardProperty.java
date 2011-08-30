package FESI.Data;

public interface StandardProperty {

    static final String LENGTHstring = "length";
    static final int LENGTHhash = LENGTHstring.hashCode();
    static final String ARGUMENTSstring = "arguments";
    static final int ARGUMENTShash = ARGUMENTSstring.hashCode();
    static final String CALLEEstring = "callee";
    static final int CALLEEhash = CALLEEstring.hashCode();
    static final String PROTOTYPEstring = "prototype";
    static final int PROTOTYPEhash = PROTOTYPEstring.hashCode();
    public static final String JSONstring = "JSON";
    public static final int JSONhash = JSONstring.hashCode();
    static final String TOJSONstring = "toJSON";
    static final int TOJSONhash = TOJSONstring.hashCode();
    public static final String TOSTRINGstring = ("toString").intern();
    public static final int TOSTRINGhash = TOSTRINGstring.hashCode();
    public static final String VALUEOFstring = ("valueOf").intern();
    public static final int VALUEOFhash = VALUEOFstring.hashCode();
    public static final String WRITABLEstring = "writable";
    public static final int WRITABLEhash = WRITABLEstring.hashCode();
    public static final String ENUMERABLEstring = "enumerable";
    public static final int ENUMERABLEhash =ENUMERABLEstring.hashCode();
    public static final String CONFIGURABLEstring = "configurable";
    public static final int CONFIGURABLEhash = CONFIGURABLEstring.hashCode();
    public static final String VALUEstring = "value";
    public static final int VALUEhash = VALUEstring.hashCode();
    public static final String GETstring = "get";
    public static final int GEThash = GETstring.hashCode();
    public static final String SETstring = "set";
    public static final int SEThash = SETstring.hashCode();

}
