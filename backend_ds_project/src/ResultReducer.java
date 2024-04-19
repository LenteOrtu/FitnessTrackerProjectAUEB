import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import shared_classes.ActivityResults;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/* Receives intermediate results from workers through a shared queue between the worker connection threads, reduces
the results into the final activity results and forwards them to the appropriate user connection thread in order to
send them to the corresponding user.
 */
public class ResultReducer extends Thread{

    public final int CHUNK_SIZE = 9; // CHUNK SIZE = chunkSize + 1 cause of intermediate point.
    private int packetsLeft;
    private final Counter activeSenders;
    private final ActivityResults sharedActivityResults;
    private final Queue<ActivityResults> intermediateResultsQueue;
    private final Queue<ActivityResults> sharedResultsQueue;
    private final WorkerIndex workerIndex;
    private final ArrayList<Queue<GPXPacket>> workerQueues;
    private final UserPacket userPacket;

    public ResultReducer(WorkerIndex workerIndex, Counter activeSenders, ActivityResults sharedActivityResults, Queue<ActivityResults> intermediateResultsQueue,
    Queue<ActivityResults> sharedResultsQueue, ArrayList<Queue<GPXPacket>> workerQueues, UserPacket userPacket){
        this.workerIndex = workerIndex;
        this.activeSenders = activeSenders;
        this.sharedActivityResults = sharedActivityResults;
        this.intermediateResultsQueue = intermediateResultsQueue;
        this.sharedResultsQueue = sharedResultsQueue;
        this.workerQueues = workerQueues;
        this.userPacket = userPacket;
    }

    @Override
    public void run(){

        // Send GPX chunks to workers in round robin fashion.
        packetsLeft = sendGPXChunks(userPacket.CONNECTION_ID, userPacket.getGpxFile());
        activeSenders.decrease();

        Queue<ActivityResults> resultsQueue = new LinkedList<>();
        while (true){
            synchronized (intermediateResultsQueue){
                if (!intermediateResultsQueue.isEmpty()){
                    ActivityResults intermediateResults  = intermediateResultsQueue.remove();
                    resultsQueue.add(intermediateResults);
                    packetsLeft -= 1;
                    if (packetsLeft == 0) break;

                }else{
                    try {
                        intermediateResultsQueue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Reduce intermediate results into final activity results.
        ActivityResults activityResults = reduce(resultsQueue);

        // Send final results to user connection so that it can forward them to the corresponding user.
        synchronized (sharedActivityResults){
            sharedActivityResults.copy(activityResults);
            sharedActivityResults.notify(); // Notify user connection that final results have been received.
        }

        // Add activity results to global stats updater thread's queue in order to update the global activity stats file.
        synchronized (sharedResultsQueue){
            sharedResultsQueue.add(activityResults);
            sharedResultsQueue.notifyAll();
        }

    }

    /* Receives list of values (Activity Results) and returns
    final activity results. */
    private ActivityResults reduce(Queue<ActivityResults> intermediateResults){
        double finalTotalTime = 0;
        double finalTotalAscent = 0;
        double finalTotalDistance = 0;

        for (ActivityResults r : intermediateResults){
            finalTotalDistance += r.getTotalDistance();
            finalTotalAscent += r.getTotalAscent();
            finalTotalTime += r.getTotalTime();
        }

        double meanSpeed = finalTotalDistance / (finalTotalTime / 60);

        return new ActivityResults(finalTotalDistance, finalTotalAscent, finalTotalTime, meanSpeed);
    }


    /* Parses GPX file and sends Worker Packets that contain CONNECTION_ID and an arraylist that contains
    chunkSize (or total points % chunkSize) consecutive gps points from the gpx file. Also returns the number of chunks
    sent so that "reduce thread" knows when to reduce intermediate results into final results. */
    private int sendGPXChunks(int CONNECTION_ID, File gpxFile){

        int chunksSent = 0;

        try{
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(gpxFile);

            // Get all GPS points from GPX file.
            NodeList points = doc.getElementsByTagName("wpt");
            gpxFile.delete(); // delete temporary gpx file from directory after you've extracted the gpx points.

            for (int i = 0; i < points.getLength(); i += CHUNK_SIZE){
                ArrayList<GPXPoint> gpxPoints = new ArrayList<>();

                int j = i;
                for (; j < i + CHUNK_SIZE && j < points.getLength(); j++){
                    GPXPoint gpxPoint = createGpxPoint(points.item(j));
                    gpxPoints.add(gpxPoint);
                }

                // Add "intermediate point" for gpx chunks.
                if (j < points.getLength()) {gpxPoints.add(createGpxPoint(points.item(j)));}

                // No need to send one point.
                if (gpxPoints.size() <= 1){
                    return chunksSent;
                }

                // Send Worker Packet to output stream connected with worker.
                synchronized (workerQueues){
                    if (workerQueues.isEmpty()){
                        workerQueues.wait();
                    }
                    // Synchronization unnecessary since thread has lock on workerQueues shared object. Just for extra safety.
                    synchronized (workerIndex){
                        int k = workerIndex.getNext(workerQueues.size());
                        synchronized (workerQueues.get(k)){
                            // Create gpx packet and add it to worker connection queue in order to be sent to corresponding worker.
                            workerQueues.get(k).add(new GPXPacket(CONNECTION_ID, gpxPoints));
                            chunksSent += 1;
                            workerQueues.get(k).notify();
                        }
                    }
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return chunksSent;
    }

    private GPXPoint createGpxPoint(Node item){
        Element point = (Element) item;
        double lat = Double.parseDouble(point.getAttribute("lat"));
        double lon = Double.parseDouble(point.getAttribute("lon"));
        double elev = Double.parseDouble(point.getElementsByTagName("ele").item(0).getTextContent());
        String time = point.getElementsByTagName("time").item(0).getTextContent();

        return new GPXPoint(lat, lon, elev, time);
    }
}









