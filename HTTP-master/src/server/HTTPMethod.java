package server;

/**
 * An ENUM describing the HTTP command.
 */
public enum HTTPMethod {
    HEAD("HEAD"),
    GET("GET"),
    PUT("PUT"),
    POST("POST");

    private final String name;

    private HTTPMethod(String s) {
        this.name = s;
    }


    @Override
    public String toString() {
        return this.name;
    }
}
