import java.io.*;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Queue;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import shared_classes.ActivityResults;

public class StatLogger {

    /* Logs user's results from a gpx file that represents a route. (Total Activity Time, Total Distance, Total Ascent, Mean Speed).
    The file that holds the routes is called 'username_route_stats'.*/
    public void logRouteStats(String username, ActivityResults activityResults) throws ParserConfigurationException, IOException, SAXException, TransformerException {

        // Check if \\user_route_stats directory exists, if not create one.
        File dir = new File(System.getProperty("user.dir") + "\\user_route_stats");
        if (!dir.exists()){
            dir.mkdirs();
        }

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc;

        String filepath = System.getProperty("user.dir") + "/user_route_stats/" + username + "_route_stats";
        File xmlFile = new File(filepath);

        if (!xmlFile.exists()){
            // If the XML file does not exist yet, create a new one.
            doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("userStatsList");
            doc.appendChild(rootElement);
        }else{
            // If the XML file already exists, parse it and use the existing root element.
            doc = docBuilder.parse(xmlFile);
        }

        Element userStatsElement = doc.createElement("userStats");
        Element totalDistanceElement = doc.createElement("totalDistance");
        Element totalAscentElement = doc.createElement("totalAscent");
        Element totalTimeElement = doc.createElement("totalTime");
        Element meanSpeedElement = doc.createElement("meanSpeed");

        totalDistanceElement.appendChild(doc.createTextNode(String.valueOf(activityResults.getTotalDistance())));
        totalAscentElement.appendChild(doc.createTextNode(String.valueOf(activityResults.getTotalAscent())));
        totalTimeElement.appendChild(doc.createTextNode(String.valueOf(activityResults.getTotalTime())));
        meanSpeedElement.appendChild(doc.createTextNode(String.valueOf(activityResults.getMeanSpeed())));

        userStatsElement.appendChild(totalDistanceElement);
        userStatsElement.appendChild(totalAscentElement);
        userStatsElement.appendChild(totalTimeElement);
        userStatsElement.appendChild(meanSpeedElement);

        Node rootElement = doc.getDocumentElement();
        rootElement.appendChild(userStatsElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        // Pretty printing. Not necessary.
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filepath));
        transformer.transform(source, result);

    }


    public void updateGlobalTotalStats(Queue<ActivityResults> activityResultsQueue) throws IOException{

        String filepath = System.getProperty("user.dir") + "\\global_stats";

        File activityStatsFile = new File(filepath);
        boolean firstEntry = false;

        // Create file if it doesn't exist. (first route log)
        if (!activityStatsFile.exists()){
            activityStatsFile.createNewFile();
            firstEntry = true;
        }

        int totalRoutes = 0;
        double meanActivityTime = 0;
        double meanDistance = 0;
        double meanAscent = 0;


        try {
            // If it is not first entry retrieve previous stats.
            if (!firstEntry){

                BufferedReader br = new BufferedReader(new FileReader(activityStatsFile));
                String line = br.readLine();
                if (line == null) return;
                String[] parts = line.split(",");

                br.close();

                // Previous stats.
                totalRoutes = Integer.parseInt(parts[0]);
                meanActivityTime = Double.parseDouble(parts[1]);
                meanDistance = Double.parseDouble(parts[2]);
                meanAscent = Double.parseDouble(parts[3]);
            }


            // Update stats.
            double totalTime = 0;
            double totalDistance = 0;
            double totalAscent = 0;
            int routes = 0;
            while (!activityResultsQueue.isEmpty()){
                routes += 1;
                ActivityResults activityResults = activityResultsQueue.remove();
                totalTime += activityResults.getTotalTime();
                totalDistance += activityResults.getTotalDistance();
                totalAscent += activityResults.getTotalAscent();
            }

            totalRoutes += routes;
            meanActivityTime = (meanActivityTime * (totalRoutes-1) + totalTime)/ totalRoutes;
            meanDistance = (meanDistance * (totalRoutes-1) + totalDistance) / totalRoutes;
            meanAscent = (meanAscent * (totalRoutes-1) + totalAscent) / totalRoutes;


            // Update file.
            String timeUpdated = ZonedDateTime.now().toString();
            String updatedStats = totalRoutes + "," + meanActivityTime + "," + meanDistance + "," + meanAscent + "," + timeUpdated + "\n";

            BufferedWriter bw = new BufferedWriter(new FileWriter(activityStatsFile));
            bw.write(updatedStats);

            bw.close();
        } catch (IOException ex) {
            System.out.println("Error writing file: " + ex.getMessage());
        }
    }


    public void updateUserTotalStats(String username, ActivityResults activityResults) throws IOException {

        // Check if \\user_total_stats directory exists, if not create one.
        File dir = new File(System.getProperty("user.dir") + "\\user_total_stats");
        if (!dir.exists()){
            dir.mkdirs();
        }

        // Check if \\user_mean_stats directory exists, if not create one.
        dir = new File(System.getProperty("user.dir") + "\\user_mean_stats");
        if (!dir.exists()){
            dir.mkdirs();
        }

        String totalStatsFilepath = System.getProperty("user.dir") + "/user_total_stats/" + username + "_user_total_stats";
        String meanStatsFilepath = System.getProperty("user.dir") + "/user_mean_stats/" + username + "_user_mean_stats";

        File totalStatsFile = new File(totalStatsFilepath);
        File meanStatsFile = new File(meanStatsFilepath);
        boolean firstEntry = false;

        // Create files if one of them doesn't exist - no need to check both. (first route log)
        if (!totalStatsFile.exists()){
            totalStatsFile.createNewFile();
            meanStatsFile.createNewFile();
            firstEntry = true;
        }

        int totalRoutes = 0;
        double totalActivityTime = 0;
        double totalDistance = 0;
        double totalAscent = 0;

        try {
            // If it is not first entry retrieve previous stats. (Just need to retrieve total stats)
            if (!firstEntry){

                BufferedReader br = new BufferedReader(new FileReader(totalStatsFile));
                String line = br.readLine();
                if (line == null) return;
                String[] parts = line.split(",");

                br.close();

                // Previous stats.
                totalRoutes = Integer.parseInt(parts[0]);
                totalActivityTime = Double.parseDouble(parts[1]);
                totalDistance = Double.parseDouble(parts[2]);
                totalAscent = Double.parseDouble(parts[3]);
            }


            // Update stats.
            totalRoutes += 1;
            totalActivityTime += activityResults.getTotalTime();
            totalDistance += activityResults.getTotalDistance();
            totalAscent += activityResults.getTotalAscent();

            // Calculate mean values.
            double meanActivityTime = totalActivityTime / totalRoutes;
            double meanDistance = totalDistance / totalRoutes;
            double meanAscent = totalAscent / totalRoutes;

            // Update files.
            String updatedTotalStats = totalRoutes + "," + totalActivityTime + "," + totalDistance + "," + totalAscent;
            String updatedMeanStats = totalRoutes + "," + meanActivityTime + "," + meanDistance + "," + meanAscent;

            BufferedWriter bwTotal = new BufferedWriter(new FileWriter(totalStatsFile));
            bwTotal.write(updatedTotalStats);

            BufferedWriter bwMean = new BufferedWriter(new FileWriter(meanStatsFile));
            bwMean.write(updatedMeanStats);

            bwTotal.close();
            bwMean.close();
        } catch (IOException ex) {
            System.out.println("Error writing file: " + ex.getMessage());
        }
    }

    // Callee needs to acquire write lock for thread-safe use of method.
    public void logUsername(String username) throws IOException {
        String filepath = System.getProperty("user.dir") + "\\usernames";
        File usernamesFile = new File(filepath);

        // Create file if it doesn't exist. (first route log)
        if (!usernamesFile.exists()){
            usernamesFile.createNewFile();
        } else{
            // Check if username already exists.
            StatExtractor statExtractor = new StatExtractor();
            HashSet<String> usernames = statExtractor.extractUsernames();

            if (usernames.contains(username)){
                return;
            }
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(usernamesFile, true));
        bw.write(username + ",");

        bw.close();
    }

}












