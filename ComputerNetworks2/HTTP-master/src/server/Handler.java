package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * A class handling the actions the server side of this project should do, including a method run().
 */
public class Handler implements Runnable {

    private Socket clientSocket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    /**
     * The constructor for the handler class. It requires the client socket.
     * @param clientSocket
     * @throws IOException
     */
    public Handler(Socket clientSocket) throws IOException {
        this.clientSocket = clientSocket;
        this.inputStream = new DataInputStream(this.clientSocket.getInputStream());
        this.outputStream = new DataOutputStream(this.clientSocket.getOutputStream());
    }

    /**
     * The method that handles client requests.
     */
    @Override
    public void run() {
        boolean shouldClose = false;
        while (! shouldClose) {
            Request request;
            try {
                request = createRequest();
            } catch (Exception e) {
                e.printStackTrace();
                Response response = new Response(null);
                try {
                    this.outputStream.write(response.getResponseData());
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                close();
                return;
            }
            if (request != null) {
                Response response = new Response(request);
                System.out.println(response);
                try {
                    this.outputStream.write(response.getResponseData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                shouldClose = response.shouldClose();
            } else {
                shouldClose = true;
            }
        }
        this.close();
    }

    /**
     * Creating a server request, reading character per character. This is necessary for the character counting.
     * @return
     * @throws Exception
     */
    private Request createRequest() throws Exception {
        Request request = new Request();
        // Read character by character because:
        //  1) inputStream.readLine() strips the \r and \n which are necessary for character counting
        //  2) the last line doesn't necessarily end with a \n, so inputStream.readLine() doesn't read it.
        while (! request.headFinished()) {
            Character nextChar = null;
            String line = "";
            while (this.clientSocket.isConnected() && (nextChar == null || nextChar != '\n')) {
                if (! clientSocket.isConnected())
                    return null;
                int charInt = inputStream.read();
                if (charInt == -1)
                    return null;
                nextChar = (char) charInt;
                line += nextChar;
            }
            request.interpretHead(line);
        }
        if (this.clientSocket.isConnected() && ! request.isFinished()) {
            final int contentLength = request.getContentLength();
            byte[] data = new byte[contentLength];
            this.inputStream.readFully(data, 0, contentLength);
            request.setData(data);
        }
        if (! this.clientSocket.isConnected()) {
            return null;
        }
        return request;
    }

    /**
     * close the client socket;
     */
    private void close() {
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
