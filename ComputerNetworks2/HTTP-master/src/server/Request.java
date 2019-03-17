package server;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * A class representing the server's request.
 */
public class Request {

    private boolean headerEnded = false;
    private boolean messageEnded = false;
    private HTTPMethod method;
    private URI requestedFile;
    private String version;
    private byte[] data;
    private Map<String, String> headers = new HashMap<>();
    private int contentLength;
    private String firstLine;


    /**
     * Filling in the properties of this request.
     * @param line
     * @throws Exception
     */
    public void interpretHead(String line) throws Exception {

        if (this.messageEnded || this.headerEnded)
            return;

        // if the reasonPhrase is still null, the first line still has to be read.
        if (this.firstLine == null) {
            if (line.trim().equals(""))
                return;
            String[] components = line.split(" ");
            this.method = HTTPMethod.valueOf(components[0].trim());
            if (components[1].equals("/"))
                this.requestedFile = new URI("index.html");
            else
                this.requestedFile = new URI(components[1]);
            this.version = components[2].split("/")[1].trim();
            if (! this.version.equals("1.0") && ! this.version.equals("1.1"))
                throw new IllegalArgumentException("Illegal version given");
            this.firstLine = line.trim();
        } else {
            if (line.trim().equals("")) {
                this.headerEnded = true;

                // If the method was HEAD, there isn't any data, so the message has ended.
                if (this.method == HTTPMethod.HEAD)
                    this.messageEnded = true;

                // If there is'nt a header "content-length", there is no body, so the message has ended.
                if (!this.headers.containsKey("content-length")) {
                    this.contentLength = 0;
                    this.messageEnded = true;
                }

                // Else, if the content-length equals 0, there is no body, sot he message has ended.
                else if (Integer.parseInt(this.headers.get("content-length")) == 0) {
                    this.messageEnded = true;
                    this.contentLength = 0;
                }

                // If the message hasn't ended yet, set the content length to the header value.
                if (!this.messageEnded) {
                    this.contentLength = Integer.parseInt(this.headers.get("content-length"));
                }
            } else {
                // Add header
                line = line.trim();
                String[] components = line.split(":", 2);
                headers.put(components[0].toLowerCase(), components[1].trim());
            }
        }
    }

    public HTTPMethod getMethod() {
        return method;
    }

    public URI getRequestedFile() {
        return requestedFile;
    }

    public String getVersion() {
        return version;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public int getContentLength() {
        return contentLength;
    }

    public boolean isFinished() {
        return messageEnded;
    }

    public boolean headFinished() {
        return headerEnded;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public String getFirstLine() {
        return this.firstLine;
    }
}
