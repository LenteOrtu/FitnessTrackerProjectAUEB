package shared_classes;

import java.io.Serializable;

public class ActivityResults implements Serializable {
    // Contains the activity results of the user.
    private double totalDistance; // Distance in km.
    private double totalAscent; // Total ascent in meters.
    private double totalTime; // Total time in minutes.
    private double meanSpeed; // Mean speed in km/h.
    private String username; // Username of user who requested activity results.

    public ActivityResults(){
        this.totalAscent = 0;
        this.totalDistance = 0;
        this.totalTime = 0;
        this.meanSpeed = 0;
        this.username = "";
    }

    public ActivityResults(double totalDistance, double totalAscent, double totalTime, double meanSpeed){
        this.totalDistance = totalDistance;
        this.totalTime = totalTime;
        this.totalAscent = totalAscent;
        this.meanSpeed = meanSpeed;
    }

    public boolean isEmpty(){
        return !((totalDistance != 0) && (totalAscent != 0) && (totalTime != 0) && (meanSpeed != 0));
    }

    public String getResults(){
        return "Total distance=" + totalDistance + " Total ascent=" + totalAscent + " Total time=" + totalTime
                + " Mean speed=" + meanSpeed;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public double getTotalAscent() {
        return totalAscent;
    }

    public void setTotalAscent(double totalAscent) {
        this.totalAscent = totalAscent;
    }

    public double getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(double totalTime) {
        this.totalTime = totalTime;
    }

    public double getMeanSpeed() {
        return meanSpeed;
    }

    public void setMeanSpeed(double meanSpeed) {
        this.meanSpeed = meanSpeed;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getUsername(){
        return username;
    }

    public void copy(ActivityResults other){
        this.totalDistance = other.totalDistance;
        this.totalAscent = other.totalAscent;
        this.totalTime = other.totalTime;
        this.meanSpeed = other.meanSpeed;
    }

}
