package cs3200.animelist;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * Network related methods
 */
public class Network {

    /**
     * Connect to a url and retrieve page data
     *
     * @param uri URL to connect to
     * @return The HTML for the page
     * @throws IOException If the page does not exist
     */
    public String connect_url(String uri) throws IOException {
        URLConnection urlc = new URL("http://www.myanimelist.net" + uri).openConnection();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String str;
        while ((str = buffer.readLine()) != null) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * Search by anime ID
     *
     * @param id ID of the anime to look up
     * @return HTML for the page
     * @throws IOException IF the page does not exist
     */
    public String searchById(int id) throws IOException {
        String searchUrl = "/anime/" + Integer.toString(id);
        return new Network().connect_url(searchUrl);
    }
}