package client;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * A class representing a HTML document.
 */
public class HTMLDocument {

    private final Response response;
    private final Document document;
    private final Connection connection;

    /**
     * The constructor for the HTLMDocument class. It requires the response and the connection and it parses the date of the response.
     * @param response
     * @param connection
     */
    public HTMLDocument(Response response, Connection connection) {
        this.response = response;
        this.document = Jsoup.parse(new String(response.getData(), StandardCharsets.ISO_8859_1));
        this.connection = connection;
    }

    /**
     * A methd that saves the information that needs to be saved for this project.
     * @throws Exception
     */
    public void saveAll() throws Exception {
        removeExisting();
        saveDependencies();
        saveMain();
    }

    /**
     * Removes an existing file that has the same name and filepath.
     */
    private void removeExisting() {
        String currentDir = System.getProperty("user.dir");
        File file = new File(currentDir + "/client-output/" + response.getUrl().getHost() + "/");
        if (file.exists()) {
            String[] entries = file.list();
            for (String s: entries) {
//                System.out.println(s);
                File currentFile = new File(file.getParent(), s);
//                System.out.println(currentFile);
//                System.out.println(Files.delete(file));
            }
            file.delete();
        }
    }

    /**
     * Saves the file to the file system.
     * @throws IOException
     */
    private void saveMain() throws IOException {
        final String currentDir = System.getProperty("user.dir");
        final URL url = response.getUrl();
        final String fileType = response.getHeaders().get("content-type")
                .split("/")[1]
                .split(";")[0];
        String path;
        if (url.getPath().equals("")) {
            path = "index";
        } else {
            path = url.getPath();
        }
        File file = new File(currentDir + "/client-output/" + url.getHost() + "/" + path + "." + fileType);

        saveFile(file, this.document.outerHtml().getBytes());
    }

    /**
     * Saves the images of the HTML document.
     * @throws Exception
     */
    private void saveDependencies() throws Exception {
        Elements images = document.getElementsByTag("img");
        Set<URI> uris = new HashSet<>(); // Make a set to not download the same uri multiple times
        final URL responseUrl = this.response.getUrl();
        
        for (Element image: images) {
        	//System.out.println(image.attr("src"));
        	
        	// replace the ads with their ad blocker equivalent.
        	if (image.attr("src").contains("ad")) {
        		if (image.attr("src").contains("1")){
        			image.removeAttr("src");
        			image.attr("src", "ad1_replacement.jpg");
        		}else if (image.attr("src").contains("2")){
        			image.removeAttr("src");
        			image.attr("src", "ad2_replacement.jpg");
        		}else if (image.attr("src").contains("3")){
        			image.removeAttr("src");
                	image.attr("src", "ad3_replacement.jpg");
        		}
        	}  
        	String src = image.attr("src").replace(" ", "%20");
            URI uri = new URI(src);
            image.attr("src", uri.getPath());
            uris.add(uri);  
        }

        for (URI uri : uris) {
        	System.out.println(uri.toString());
            if (uri.getHost() == null) {
                saveURL(new URL(responseUrl.getProtocol() + "://"
                        + responseUrl.getHost() + ":" + responseUrl.getPort() + "/" + uri.getPath()));
            } else {
                saveURL(new URL(uri.toString()), responseUrl.getHost());
            }
        }
    }

    /**
     * saves the URL when there is no host name.
     * @param url
     * @throws Exception
     */
    private void saveURL(URL url) throws Exception {
        saveURL(url, null);
    }

    /**
     * Saves the HTML page to a file.
     * @param url
     * @param hostName
     * @throws Exception
     */
    private void saveURL(URL url, String hostName) throws Exception {
        Request request = new Request(HTTPMethod.GET, url, "1.1", "");
        Response response;
        if (!this.connection.getHost().equals(request.getUrl().getHost())) {
            Connection newConnection = new Connection(request.getUrl().getHost(), this.connection.getPort());
            response = newConnection.get(request);
        } else {
            response = this.connection.get(request);
        }
        if (! response.isSuccess())
            return;
//        String fileType = response.getHeaders().get("content-type").split("/")[1];

        String currentDir = System.getProperty("user.dir");
        File file;
        if (hostName == null) {
            file = new File(currentDir + "/client-output/" + url.getHost() + "/" + url.getPath());
        } else {
            file = new File(currentDir + "/client-output/" + hostName + "/" + url.getPath());
        }

        saveFile(file, response.getData());

    }

    /**
     * Saves the given data in the given file.
     * @param file
     * @param data
     * @throws IOException
     */
    private void saveFile(File file, byte[] data) throws IOException {
//        System.out.println(new String(data));
        file.getParentFile().mkdirs();
        FileOutputStream writer = new FileOutputStream(file);

        writer.write(data);
        writer.close();
    }
}
