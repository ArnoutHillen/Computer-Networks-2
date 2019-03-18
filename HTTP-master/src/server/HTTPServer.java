package server;

import java.net.*;

/**
 * The server side of this TCP project.
 */
public class HTTPServer {

    /**
     * The main method for the server side of this project, reading and saving the necessary information.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 1) {
            throw new IllegalArgumentException(); // Only the port number should be provided
        }

        int portNumber = Integer.parseInt(args[0]);
        ServerSocket socket = new ServerSocket(portNumber);
        System.out.println("Listening on port " + portNumber + "...");

        //launch new thread for every client
        while (true) {
            Socket connectionSocket = socket.accept();
            if (connectionSocket != null) {
                Handler request = new Handler(connectionSocket);
                Thread thread = new Thread(request);
                thread.start();
            }
        }

    }

}
