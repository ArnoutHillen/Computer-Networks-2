package server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A class representing the response on the server's side.
 */
public class Response {

    private static final File NOT_FOUND_FILE = new File(System.getProperty("user.dir") + "/HTTP-master/res/404.html");
    private static final File BAD_REQUEST_FILE = new File(System.getProperty("user.dir") + "/HTTP-master/res/400.html");
    private static final String SERVER_ROOT = "webpage";
    private static final String PUT_DIRECTORY = "server-put";
    private static final String POST_DIRECTORY = "server-post";

    private Request request;

    private ResponseCode responseCode;
    private Map<String, String> headers = new HashMap<>();
    private byte[] responseData;

    /**
     * The constructor for the request class. It requires the (server) request. The actual handling of the request happens
     * in the create() method.
     * @param request
     */
    public Response(Request request) {
        this.request = request;
        this.create();
    }

    /**
     * A method handles the (server) request.
     */
    private void create() {

        if (this.request == null) {
            this.throwBadRequest();
            return;
        }

        if (this.request.getVersion().equals("1.1") && ! this.request.getHeaders().containsKey("host")) {
            this.throwBadRequest();
            return;
        }

        String currentDir = System.getProperty("user.dir");
        File file = new File(currentDir + "/" + SERVER_ROOT + "/" + request.getRequestedFile().getPath());


        switch (request.getMethod()) {
            case HEAD:
                try {
                    if (this.check304(file)) {
                        createDefaultHeaders();
                        this.responseCode = ResponseCode.NOT_MODIFIED;
                        this.responseData = getHeader().getBytes(StandardCharsets.ISO_8859_1);
                    } else {
                        if (!file.exists()) {
                            file = NOT_FOUND_FILE;
                            this.responseCode = ResponseCode.NOT_FOUND;
                        } else {
                            this.responseCode = ResponseCode.OK;
                        }
                        createHeaders(file);
                        this.responseData = getHeader().getBytes(StandardCharsets.ISO_8859_1);
                        break;
                    }
                } catch (Exception e) {
                    throwServerError(e);
                    e.printStackTrace();
                }
            case GET:
                try {
                    if (this.check304(file)) {
                        this.responseCode = ResponseCode.NOT_MODIFIED;
                        this.responseData = getHeader().getBytes(StandardCharsets.ISO_8859_1);
                    } else {
                        if (!file.exists()) {
                            file = NOT_FOUND_FILE;
                            this.responseCode = ResponseCode.NOT_FOUND;
                        } else {
                            this.responseCode = ResponseCode.OK;
                        }
                        this.responseData = getResult(file);
                    }
                } catch (Exception e) {
                    throwServerError(e);
                    e.printStackTrace();
                }
                break;
            case PUT:
                handlePut();
                break;
            case POST:
                handlePost();
                break;
        }
    }

    /**
     * Handles the put command, saving the files in a directory.
     */
    private void handlePut() {
        String currentDir = System.getProperty("user.dir");
        File file;
        file = new File(currentDir + "/" + PUT_DIRECTORY + "/" + request.getRequestedFile().getPath());
        //file.getParentFile().mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(this.request.getData());
            fos.close();
            this.responseCode = ResponseCode.OK;
            this.responseData = getResult("Saved successfully!");
        } catch (IOException e) {
            throwServerError(e);
            e.printStackTrace();
        }
    }

    /**
     * Handles the post command, saving the files in a directory, appending to the existing file.
     */
    private void handlePost() {
        String currentDir = System.getProperty("user.dir");
        File file;
        file = new File(currentDir + "/" + POST_DIRECTORY + "/" + request.getRequestedFile().getPath());
        file.getParentFile().mkdirs();
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            fos.write(this.request.getData());
            fos.write((int) '\n');
            fos.close();
            this.responseCode = ResponseCode.OK;
            this.responseData = getResult("Saved successfully!");
        } catch (IOException e) {
            throwServerError(e);
            e.printStackTrace();
        }
    }

    /**
     * Returns the joint of the header and the data for the file.
     * @param file
     * @return
     * @throws IOException
     */
    private byte[] getResult(File file) throws IOException {
        createHeaders(file);
        return concat(getHeader().getBytes(StandardCharsets.ISO_8859_1), getFile(file));
    }

    /**
     * Returns the joint of the header and the data for a given string body.
     * @param body
     * @return
     */
    private byte[] getResult(String body) {
        createHeaders(body);
        return (getHeader() + body).getBytes(StandardCharsets.ISO_8859_1);
    }

    /**
     * Throws a server error.
     * @param e
     */
    private void throwServerError(Exception e) {
        this.responseCode = ResponseCode.SERVER_ERROR;
        this.responseData = getResult(e.getMessage());
    }

    /**
     * A method that throws a bad request.
     */
    private void throwBadRequest() {
        this.responseCode = ResponseCode.BAD_REQUEST;
        try {
            this.responseData = getResult(BAD_REQUEST_FILE);
        } catch (IOException e) {
            throwServerError(e);
            e.printStackTrace();
        }
    }

    /**
     * checks whether a 304 should be thrown, taking the 'if-modified-since' header in account.
     * @param file
     * @return
     * @throws ParseException
     */
    private boolean check304(File file) throws ParseException {
        if (this.request.getHeaders().containsKey("if-modified-since")) {
            String pattern = "EEE, dd MMM yyyy HH:mm:ss z";
            SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            Date modifiedSince = format.parse(this.request.getHeaders().get("if-modified-since"));
            Date fileModified = new Date(file.lastModified());
            return fileModified.after(modifiedSince);
        } else {
            return false;
        }
    }

    /**
     * Formats the given date to the "EEE, dd MMM yyyy HH:mm:ss z" format.
     * @param date
     * @return
     */
    private static String getFormattedDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US
        );
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(date);
    }

    /**
     * Formats the time at this moment to the "EEE, dd MMM yyyy HH:mm:ss z" format.
     * @return
     */
    private static String getFormattedDate() {
        return getFormattedDate(Calendar.getInstance().getTime());
    }

    /**
     * Returns the byte[] format of the given file.
     * @param file
     * @return
     * @throws IOException
     */
    private static byte[] getFile(File file) throws IOException {
        return Files.readAllBytes(Paths.get(file.toString()));
    }

    /**
     * Creates the headers for the given file.
     * @param file
     */
    private void createHeaders(File file) {

        if (file.exists()) {
            this.headers.put("Content-Length", Long.toString(file.length()));
            String[] components = file.getPath().split("\\.");
            String fileType = components[components.length - 1];
            switch (fileType) {
                case "html":
                    this.headers.put("Content-Type", "text/html");
                    break;
                case "jpg":
                    this.headers.put("Content-Type", "image/jpg");
                    break;
                case "jpeg":
                    this.headers.put("Content-Type", "image/jpeg");
                    break;
                case "png":
                    this.headers.put("Content-Type", "image/png");
                    break;
            }
            if (this.responseCode != ResponseCode.NOT_FOUND)
                this.headers.put("Last-Modified", getFormattedDate(new Date(file.lastModified())));
        }

        createDefaultHeaders();
    }

    /**
     * Initialize header parameters based on a message body of plain text, given as argument.
     * @param body The body to create the header for.
     */
    private void createHeaders(String body) {
        this.headers.put("Content-Length", Integer.toString(body.getBytes(StandardCharsets.ISO_8859_1).length));
        this.headers.put("Content-Type", "text/plain");

        createDefaultHeaders();

    }

    /**
     * Creates the default headers of this response.
     */
    private void createDefaultHeaders() {
        this.headers.put("Date", getFormattedDate());
    }

    /**
     * Builds the header of the request.
     * @return
     */
    private String getHeader() {
        StringBuilder result = new StringBuilder();
        result.append("HTTP/");
        if (request == null) {
            result.append("1.1");
        } else {
            result.append(request.getVersion());
        }
        result.append(" ")
                .append(this.responseCode.getCode())
                .append(" ")
                .append(this.responseCode.getPhrase())
                .append("\r\n");
        for (Map.Entry<String, String> header : headers.entrySet()) {
            result.append(header.getKey())
                    .append(": ")
                    .append(header.getValue())
                    .append("\r\n");
        }
        result.append("\r\n");
        return result.toString();
    }

    public byte[] getResponseData() {
        return responseData;
    }

    /**
     * Checks whether the connection should close.
     * @return
     */
    public boolean shouldClose() {
        if (this.request.getVersion().equals("1.0"))
            return true;
        if (this.request.getHeaders().containsKey("connection"))
            if (this.request.getHeaders().get("connection").contains("close"))
                return true;
        return false;
    }

    /**
     * Join the components of the given array.
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

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("[")
                .append(getFormattedDate())
                .append("] \"")
                .append(request.getFirstLine())
                .append("\" ")
                .append(this.responseCode.getCode())
                .append(" ")
                .append(this.responseCode.getPhrase());
        return result.toString();
    }
}
