package client;

import org.omg.SendingContext.RunTime;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * A class representing the response on the client's side.
 */
public class Response {

    private final Request request;

    private boolean headerEnded = false;
    private boolean messageEnded = false;
    private boolean success;
    private int responseCode; // The response code, ex. 200
    private String reasonPhrase; // The phrase after the response code, ex. OK
    private byte[] data;
    private Map<String, String> headers = new HashMap<>();
    private int contentLength;
    private boolean isChunked;

    /**
     * The constructor for the request class. It requires the methond name, url, HTTP version and data and it adds the necessary
     * information to the headers.
     * @param request
     */
    public Response(Request request) {
        this.request = request;
    }

    /**
     * A method that interprets the given line from the connection.
     * @param line
     */
    public void interpretHead(String line) {

        if (this.messageEnded || this.headerEnded)
            return;

        // if the reasonPhrase is still null, the first line still has to be read.
        if (this.reasonPhrase == null) {
            String[] components = line.split(" ");
            this.responseCode = Integer.parseInt(components[1].trim());
            this.reasonPhrase = components[2].trim();
        } else {
            if (line.trim().equals("")) {
                this.headerEnded = true;

                // If the method was HEAD, there isn't any data, so the message has ended.
                if (this.request.getMethod() == HTTPMethod.HEAD)
                    this.messageEnded = true;

                // If there is'nt a header "content-length", there is no body, so the message has ended.
                if (!this.headers.containsKey("content-length") && !this.headers.containsKey("transfer-encoding")) {
                	this.contentLength = 0;
                    this.messageEnded = true;	
                }
                // Else, if the content-length equals 0, there is no body, so he message has ended.
                else if (this.headers.containsKey("content-length") && 
                		Integer.parseInt(this.headers.get("content-length")) == 0) {
                    this.messageEnded = true;
                    this.contentLength = 0;
                }

                if (this.getResponseCode() != 200) {
                    if (this.getResponseCode() == 404) {
//                        System.out.println("Failed to load resource; the server responded with 404 for file " + this.getUrl());
                    }
                    this.success = false;
                } else {
                    this.success = true;
                }

                // If the message hasn't ended yet, check for transfer-encoding. Otherwise set the content length.
                if (!this.messageEnded) {
                	
                	// check for transfer-encoding header
                	if (this.headers.containsKey("transfer-encoding")) {
                		this.isChunked = true;
                	}else {
                        this.contentLength = Integer.parseInt(this.headers.get("content-length"));
                	}
                }
            } else {
                // Add header
                line = line.trim();
                String[] components = line.split(":", 2);
                headers.put(components[0].toLowerCase(), components[1].trim());
            }
        }
    }
    
    public void interpretBody(String line){
    	
    }
    
    public boolean isChunked(){
    	return this.isChunked;
    }

    public String getReasonPhrase() {
        return reasonPhrase;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public boolean isFinished() {
        return this.messageEnded;
    }

    public boolean headFinished() {
        return this.headerEnded;
    }

    public int getContentLength() {
        return contentLength;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public URL getUrl() {
        return this.request.getUrl();
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.getResponseCode())
                .append(" ")
                .append(this.getReasonPhrase())
                .append("\n");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            result.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\n");
        }


        result.append("\n");
        if (this.getData() != null)
            result.append(new String(this.getData(), StandardCharsets.ISO_8859_1))
                    .append("\n");
        return result.toString();
    }
}
