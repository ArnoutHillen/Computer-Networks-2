package server;

/**
 * An ENUM describing the response code.
 */
public enum ResponseCode {
    OK(200, "OK"),
    NOT_FOUND(404, "Not Found"),
    SERVER_ERROR(500, "Internal Server Error"),
    NOT_MODIFIED(304, "Not Modified"),
    BAD_REQUEST(400, "Bad Request");

    private final int code;
    private final String phrase;

    /**
     * The constructor for the response code.
     * @param code
     * @param phrase
     */
    private ResponseCode(int code, String phrase) {
        this.code = code;
        this.phrase = phrase;
    }

    public int getCode() {
        return code;
    }

    public String getPhrase() {
        return phrase;
    }

    @Override
    public String toString() {
        return Integer.toString(this.code) + " " + this.phrase;
    }
}
