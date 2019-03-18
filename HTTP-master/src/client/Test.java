package client;

import java.net.URI;
import java.net.URISyntaxException;

public class Test {
    public static void main(String[] args) throws Exception {
        URI test = new URI("http://scouts.be/images/logo_gsb.jpg");
        System.out.println(test.getPath());
    }
}
