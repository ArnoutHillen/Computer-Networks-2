package client;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Map;

/**
 * A class representing the connection on the client's side.
 */
public class Connection {

    private Socket socket;
    private final String host;
    
    private final int port;

    private final DataOutputStream outputStream;
    private final DataInputStream inputStream;

    /**
     * The constructor for the connection class. It requires the hostname and portnumber and it creates a socket for this connection.
     * @param host
     * @param port
     * @throws IOException
     */
    public Connection(String host, int port) throws IOException {
        this.host = host;
        this.port = port;
        this.socket = new Socket(this.host, this.port);
        this.outputStream = new DataOutputStream(this.socket.getOutputStream());
        this.inputStream = new DataInputStream(this.socket.getInputStream());
    }

    /**
     * Returns the response to the given request.
     * @param request
     * @return
     * @throws Exception
     */
    public Response get(Request request) throws Exception {
        if (! this.socket.isConnected()) {
            this.socket = new Socket(this.host, this.port);
        }
        byte[] requestBytes = request.toBytes();
        this.outputStream.write(requestBytes);
        Response response = new Response(request);

        // Read character by character because:
        //  1) inputStream.readLine() strips the \r and \n which are necessary for character counting
        //  2) the last line doesn't necessarily end with a \n, so inputStream.readLine() doesn't read it.
        while (! response.headFinished()) {
            char nextChar = (char) inputStream.read();
            String line = "";
            while (nextChar != '\n') {
                line += nextChar;
                nextChar = (char) inputStream.read();
            }
            response.interpretHead(line);
        }

        if (! response.isFinished()) {
            final int contentLength = response.getContentLength();
            byte[] data = new byte[contentLength];
            this.inputStream.readFully(data, 0, contentLength);
            response.setData(data);
        }

        return response;
    }

    /**
     * Closes the socket of this connection.
     * @throws IOException
     */
    public void close() throws IOException {
        this.socket.close();
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
