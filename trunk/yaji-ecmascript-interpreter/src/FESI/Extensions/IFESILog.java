package FESI.Extensions;

public interface IFESILog {

    void asDebug(String text);

    void asTrace(String text);

    void asInfo(String text);

    void asWarning(String text);

    void asError(String text);

}
