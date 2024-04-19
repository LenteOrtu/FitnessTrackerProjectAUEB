import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import shared_classes.ActivitiesStats;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;


public class StatExtractor {

    // Parses global stats file or user's total stats file and extracts stats, returns them via ActivitiesStats object.
    public ActivitiesStats extractStats(File totalStats){

        double activityTime = 0;
        double distance = 0;
        double ascent = 0;

        try {
            BufferedReader br = new BufferedReader(new FileReader(totalStats));
            String line = br.readLine();
            String[] parts = line.split(",");

            activityTime = Double.parseDouble(parts[1]);
            distance = Double.parseDouble(parts[2]);
            ascent = Double.parseDouble(parts[3]);

            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ActivitiesStats(activityTime, distance, ascent);
    }

    public String extractUsername(File gpxFile){

        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(gpxFile);

            Element rootElement = doc.getDocumentElement();

            return rootElement.getAttribute("creator"); // Return username.

        } catch (ParserConfigurationException | SAXException | IOException e){
            e.printStackTrace();
        }

        return "";
    }

    public HashSet<String> extractUsernames(){
        String filepath = System.getProperty("user.dir") + "\\usernames";
        File usernamesFile = new File(filepath);

        if (usernamesFile.exists()){
            try{
                BufferedReader br = new BufferedReader(new FileReader(usernamesFile));
                String line = br.readLine();

                return new HashSet<String>(Arrays.asList(line.split(",")));
            }catch (IOException e){
                e.printStackTrace();
            }
        }

        return new HashSet<String>();
    }

}







