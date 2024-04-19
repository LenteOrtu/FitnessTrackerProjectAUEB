import java.io.Serializable;

public class GPXPoint implements Serializable {

    private double latitude;
    private double longitude;
    private double elevation;
    private String time;

    public GPXPoint(double latitude, double longitude, double elevation, String time){
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
        this.time = time;
    }

    public double getLatitude() {
        return latitude;
    }


    public double getLongitude() {
        return longitude;
    }

    public double getElevation() {
        return elevation;
    }

    public String getTime() {
        return time;
    }

}
