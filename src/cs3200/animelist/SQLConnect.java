package cs3200.animelist;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Connects to a database to interact
 */
public class SQLConnect {

    /**
     * The name of the computer running MySQL
     */
    private static final String SERVER_NAME = "localhost";

    /**
     * The port of the MySQL server (default is 3306)
     */
    private static final int PORT_NUMBER = 3306;

    /**
     * The name of the database we are testing with
     */
    private static final String DB_NAME = "projectstore";

    /**
     * Date formatting
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);


    /**
     * The database connection
     */
    private Connection conn;

    /**
     * Create a new SQLConnect object and open a database connection
     *
     * @param userName Username to use for the connection
     * @param password Password to use for the connection
     */
    public SQLConnect(String userName, String password) {
        try {
            // Connect to the database
            this.conn = getConnection(userName, password);
        } catch (SQLException e) {
            System.err.println("ERROR: Could not connect to the database");
            e.printStackTrace();
        }
    }

    /**
     * Create a new database Connection
     *
     * @return The successful Connection
     * @throws SQLException If we get a SQL error
     */
    private Connection getConnection(String userName, String password) throws SQLException {
        Properties connectionProps = new Properties();
        connectionProps.put("user", userName);
        connectionProps.put("password", password);
        return DriverManager.getConnection("jdbc:mysql://"
                        + SERVER_NAME + ":" + PORT_NUMBER + "/" + DB_NAME,
                connectionProps);
    }

    /**
     * Runs a query and returns the result set
     *
     * @param procedure Procedure to run
     * @return The ResultSet from the database
     * @throws SQLException If we get a SQL error
     */
    private ResultSet executeProcedure(String procedure) throws SQLException {
        CallableStatement cs = conn.prepareCall("{CALL " + procedure + "}");
        return cs.executeQuery();
    }

    /**
     * Scrape all data for a specific anime from MyAnimeList
     */
    public void scrapeAnime() {
        // Scrape data for the number of shows specified in the loop
        for (int i = 1; i <= 150; i++) {
            try {
                // Get the anime webpage and initialize variables
                Document doc = Jsoup.parse(new Network().searchById(i));
                String name = "", numEps = "", dates = "", startDate = "", endDate = "", score = "",
                        rating = "", showType = "", status = "", showDesc = "", imgUrl = "";

                // Scrape data from webpage for a specific anime
                name += doc.select("[itemprop=name]").get(0).ownText();
                numEps += Utility.textIfExists(doc.select("span:containsOwn(Episodes:)"));
                dates += Utility.textIfExists(doc.select("span:containsOwn(Aired:)"));
                startDate += LocalDate.parse(dates.split(" to ")[0], FORMATTER).toString();
                endDate += LocalDate.parse(dates.split(" to ")[1], FORMATTER).toString();
                score += doc.select("[itemprop=ratingValue]").get(0).ownText();
                rating += Utility.textIfExists(doc.select("span:containsOwn(Rating:)")).split(" - ")[0];
                showType += Utility.textIfExists(doc.select("span:containsOwn(Type:)"));
                status += Utility.textIfExists(doc.select("span:containsOwn(Status:)"));
                showDesc += doc.select("[itemprop=description]").get(0).ownText();
                imgUrl += doc.select("img").get(1).absUrl("src");

                // Add scraped anime data to database
                String procedure = "insert_anime(\"" + i + "\", \"" + name + "\", \"" + numEps + "\", " +
                        "\"" + startDate + "\", \"" + endDate + "\", \"" + score + "\", \"" + rating + "\"," +
                        " \"" + showType + "\", \"" + status + "\", \"" + showDesc + "\", \"" + imgUrl + "\")";
                this.executeProcedure(procedure);

                // Add studio data to the database
                Elements studios = doc.select("span:containsOwn(Producers:)").parents().get(0).select("a");
                for (Element e : studios) {
                    procedure = "insert_studio(\"" + e.attr("href").split("producer/")[1] + "\"," +
                            " \"" + e.attr("title") + "\", \"" + i + "\")";
                    this.executeProcedure(procedure);
                }

                // Scrape character and episode data for the anime
                String episodesUrl = doc.select("a:containsOwn(More episodes)").get(0).attr("href").substring(22);
                String charactersUrl = doc.select("a:containsOwn(More characters)").get(0).attr("href");
                if (!episodesUrl.equals("")) {
                    scrapeEpisodes(i, episodesUrl);
                }
                if (!charactersUrl.equals("")) {
                    scrapeCharacters(i, charactersUrl);
                }
            } catch (Exception e) {
                // Do nothing for the time being
                // e.printStackTrace();
            }
        }
    }

    /**
     * Scrape data for a show's episodes
     *
     * @param url URL for the show's episode page
     */
    private void scrapeEpisodes(int animeId, String url) {
        try {
            // Get episodes webpage and initialize variables
            Document doc = Jsoup.parse(new Network().connect_url(url));
            Elements nodes = doc.select("h2:containsOwn(Episodes) + table").get(0).select("table").select("tr");
            // Get episode information
            for (int i = 1; i < nodes.size(); i++) {
                Element node = nodes.get(i);

                // Get episode information
                Elements episodeNodes = node.select("td");
                String episodeNum = episodeNodes.select("[class=episode-number nowrap]").text();
                String episodeName = episodeNodes.select("[class=episode-title w-break]").text();
                String airdate =
                        LocalDate.parse(episodeNodes.select("[class=episode-aired]").text(), FORMATTER).toString();

                // Add episode information to the table
                String procedure = "insert_episode(\"" + episodeName + "\", \"" + animeId + "\"," +
                        " \"" + episodeNum + "\", \"" + airdate + "\")";
                this.executeProcedure(procedure);
            }
        } catch (Exception e) {
            // Do nothing for the time being
            // e.printStackTrace();
        }
    }

    /**
     * Scrape data for a show's characters
     *
     * @param url URL for the show's character page
     */
    private void scrapeCharacters(int animeId, String url) {
        try {
            // Get the characters webpage and initialize variables
            Document doc = Jsoup.parse(new Network().connect_url(url));
            Elements nodes =
                    doc.select("h2:containsOwn(Characters & Voice Actors) + table").parents().get(0).select("table");
            Element parentDiv = doc.select("h2:containsOwn(Characters & Voice Actors)").parents().get(0);

            // Get at least the first few character entries
            for (int i = 1; i < Math.min(nodes.size() - 1, 12); i++) {
                Element node = nodes.get(i);
                if (!node.parent().equals(parentDiv)) continue; // required so the nested tables are skipped

                // Get element nodes
                Elements characterNodes = node.select("tr").get(0).select("td");
                Element characterInfoNode = characterNodes.get(1);
                Element characterImageNode = characterNodes.get(0);

                // Find character ID
                int id = Utility.idFromUrl(characterInfoNode.select("a").get(0).attr("href"));

                // Get other character information
                String name = characterInfoNode.select("a").get(0).ownText();
                String role = characterInfoNode.select("small").text();
                String[] newUrl = characterImageNode.select("a img").attr("src").split("/images");
                String imgUrl = "http://cdn.myanimelist.net/images" + newUrl[1];

                // Add character information to the table
                String procedure = "insert_character(\"" + id + "\", \"" + name + "\", \"" + animeId + "\"," +
                        " \"" + role + "\", \"" + imgUrl + "\")";
                this.executeProcedure(procedure);

                // Get voice actor information
                for (Element vaNode : node.select("table tr")) {
                    Elements infoNodes = vaNode.select("td");
                    if (infoNodes.size() > 0) {
                        Element vaInfoNode = infoNodes.get(0);
                        Integer vaId = Utility.idFromUrl(vaInfoNode.select("a").attr("href"));
                        String vaNation = vaInfoNode.select("small").text();
                        String vaName = vaInfoNode.select("a").text();

                        // Add voice actor information to the table
                        procedure = "insert_actor(\"" + vaId + "\", \"" + vaName + "\", \"" + vaNation + "\"," +
                                " \"" + id + "\")";
                        this.executeProcedure(procedure);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: Unable to get character data for " + animeId);
            e.printStackTrace();
        }
    }

    /**
     * Run the program
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            new SQLConnect(args[0], args[1]).scrapeAnime();
        } else {
            System.err.println("ERROR: Please specify database username and password as program arguments!");
        }
    }
}
