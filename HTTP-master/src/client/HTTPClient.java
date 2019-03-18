package client;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

/**
 * The client side of this TCP project, used for sending requests and receiving answers over sockets.
 */
public class HTTPClient {

    /**
     * The main method for the client side of this project, reading and saving the necessary information.
     * @param args
     *         COMMAND URI PORT(80) VERSION
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        HTTPMethod method = HTTPMethod.valueOf(args[0]);
        int port = Integer.parseInt(args[2]);
        URL url = new URL(args[1]);
        url = new URL(url.getProtocol(), url.getHost(), port, url.getFile());

        String data = "";
        if (method.equals(HTTPMethod.POST) || method.equals(HTTPMethod.PUT)) {
            Scanner reader = new Scanner(System.in);
            System.out.println("DATA: (empty line to finish)");
            String line;
            String result = "";
            while (! (line = reader.nextLine()).isEmpty()) {
                result += line;
            }
            data = result;
        }

        Connection connection = new Connection(url.getHost(), port);
        Request request = new Request(method, url, "1.1", data);
        Response response = connection.get(request);
        System.out.println(response);

        if (method == HTTPMethod.GET) {
            HTMLDocument document = new HTMLDocument(response, connection);
            document.saveAll();
        }

        connection.close();
    }
}
