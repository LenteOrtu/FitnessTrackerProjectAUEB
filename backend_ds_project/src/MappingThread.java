import shared_classes.ActivityResults;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingThread extends Thread{

    private final ObjectOutputStream oos;
    private final GPXPacket gpxPacket;

    public MappingThread(ObjectOutputStream oos, GPXPacket gpxPacket){
        this.oos = oos;
        this.gpxPacket = gpxPacket;
    }


    @Override
    public void run(){
        WorkerPacket workerPacket = map(gpxPacket.CONNECTION_ID, gpxPacket.getGpxPoints());
        synchronized (oos){
            try{
                oos.writeObject(workerPacket);
                oos.flush();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }


    /* Receives gpx chunk (gpx points) and returns worker packet containing key = CONNECTION_ID and
    value = intermediate results
    */
    public WorkerPacket map(int CONNECTION_ID, ArrayList<GPXPoint> gpxPoints){
        ActivityResults activityResults = new ActivityResults();
        activityResults.setTotalDistance(calculateTotalDistance(gpxPoints));

        activityResults.setTotalAscent(calculateTotalAscent(gpxPoints));
        activityResults.setTotalTime(calculateTotalTime(gpxPoints));

        return new WorkerPacket(CONNECTION_ID, activityResults);
    }


    // Receives arraylist of consecutive gpx points and calculates and returns total distance in km.
    private double calculateTotalDistance(ArrayList<GPXPoint> gpxPoints){
        double totalDistance = 0;
        for (int i = 1; i < gpxPoints.size(); i++){
            totalDistance += calculateDistance(gpxPoints.get(i-1),gpxPoints.get(i));
        }
        return totalDistance;
    }


    // Returns distance (in km) between two GPX points.
    private double calculateDistance(GPXPoint p1, GPXPoint p2){
        double EARTH_RADIUS = 6371;

        double dLat = Math.toRadians(p2.getLatitude() - p1.getLatitude());
        double dLon = Math.toRadians(p2.getLongitude() - p1.getLongitude());

        double lat1 = Math.toRadians(p1.getLatitude());
        double lat2 = Math.toRadians(p2.getLatitude());

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }


    private double calculateTotalAscent(ArrayList<GPXPoint> gpxPoints) {

        double totalAscent = 0;
        for (int i = 1; i < gpxPoints.size(); i++) {
            double e1 = gpxPoints.get(i-1).getElevation();
            double e2 = gpxPoints.get(i).getElevation();

            if (e2-e1 > 0) {totalAscent += e2-e1;}
        }
        return totalAscent;
    }

    // Assumes that each gpx file covers up to 24 hrs.
    private double calculateTotalTime(ArrayList<GPXPoint> gpxPoints){
        GPXPoint firstPoint = gpxPoints.get(0);
        GPXPoint lastPoint = gpxPoints.get(gpxPoints.size()-1); // different from firstPoint since gpxPoints.size() >= 2.
        double startingTimeInMinutes = extractTimeInMinutes(firstPoint.getTime());
        double endingTimeInMinutes = extractTimeInMinutes(lastPoint.getTime());

        // If day changes while runner is running:
        if (startingTimeInMinutes > endingTimeInMinutes) {endingTimeInMinutes += 24*60;}

        return endingTimeInMinutes - startingTimeInMinutes;
    }


    /* Extracts time from date-time string e.x. "2023-03-19T17:39:03Z" and converts it to minutes. */
    private double extractTimeInMinutes(String time){
        Pattern pattern = Pattern.compile("(?<=T)(\\d{2}):(\\d{2}):(\\d{2})");
        Matcher matcher = pattern.matcher(time);

        if (matcher.find()){
            String hour = matcher.group(1);
            String minute = matcher.group(2);
            String second = matcher.group(3);

            return Integer.parseInt(hour) * 60 + Integer.parseInt(minute) + ((double) Integer.parseInt(second) /60);
        }

        // Note: 1.38 mins = 1 min + 60 * 0.38 seconds

        return 0;
    }
}
