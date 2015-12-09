package cs3200.animelist;

import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class
 */
public class Utility {

    /**
     * Find the ID of a given URL string
     *
     * @param url URL to check
     * @return The ID of the page
     */
    public static Integer idFromUrl(String url) {
        Pattern idPattern = Pattern.compile("/(\\w+)/(\\d+)/?.*");
        Matcher matcher = idPattern.matcher(url);
        if (matcher.find()) {
            return Integer.valueOf(matcher.group(2));
        }
        return 0;
    }

    /**
     * Gets a node text if it exists
     *
     * @param nodes Nodes to check
     * @return Text of the Node
     */
    public static String textIfExists(Elements nodes) {
        if (nodes.size() > 0) {
            return nodes.parents().get(0).ownText();
        }
        return "";
    }
}
