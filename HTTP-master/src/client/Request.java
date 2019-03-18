package client;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * A class representing the client's request.
 */
public class Request {

    private final HTTPMethod method;
    private final URL url;
    private final String httpVersion;
    private final byte[] data;
    private final Map<String, String> headers = new HashMap<>();

    /**
     * The constructor for the request class. It requires the method name, url, HTTP version and data and it adds the necessary
     * information to the headers.
     * @param method
     * @param url
     * @param httpVersion
     * @param data
     */
    public Request(HTTPMethod method, URL url, String httpVersion, String data) {
        this.method = method;
        this.url = url;
        this.httpVersion = httpVersion;
        this.data = data.getBytes(StandardCharsets.ISO_8859_1);

        if (this.httpVersion.equals("1.1")) {
            this.headers.put("Host", this.url.getHost() + ":" + this.url.getPort());
        }

        if (this.data.length > 0) {
            this.headers.put("Content-Length", Integer.toString(this.data.length));
            this.headers.put("Content-Type", "text/plain");
        }
    }

    /**
     *
     * @param builder
     * @return
     */
    private StringBuilder appendHeaders(StringBuilder builder) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }
        return builder;
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public URL getUrl() {
        return url;
    }

    /**
     *
     * @return
     */
    private String getHeader() {
        final StringBuilder result = new StringBuilder();
        result.append(this.method.toString())
                .append(" ");
        if (! this.url.getPath().startsWith("/"))
            result.append("/");
        result.append(this.url.getPath().replace(" ", "%20"))
                .append(" ")
                .append("HTTP/")
                .append(this.httpVersion)
                .append("\r\n");
        appendHeaders(result).append("\r\n");

        return result.toString();
    }

    /**
     * Returns the header in bytes.
     * @return
     */
    public byte[] toBytes() {
        return concat(getHeader().getBytes(StandardCharsets.ISO_8859_1), this.data);
    }

    /**
     *
     * @param components
     * @return
     */
    private static byte[] concat(byte[]... components) {
        int length = 0;
        for (byte[] component : components) {
            length += component.length;
        }
        byte[] result = new byte[length];

        for (int i = 0; i < components.length; i++) {
            if (i == 0) {
                System.arraycopy(components[0], 0, result, 0, components[0].length);
            } else {
                System.arraycopy(components[i], 0, result, components[i - 1].length, components[i].length);
            }
        }
        return result;
    }
}
