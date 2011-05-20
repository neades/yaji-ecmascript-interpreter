package FESI.Extensions;

public class ExitRequest extends Error {

    private final int status;

    public ExitRequest(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    private static final long serialVersionUID = -7824162672938836056L;

}
