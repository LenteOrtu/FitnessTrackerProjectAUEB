package shared_classes;
import java.io.Serializable;

public class ActivitiesStats implements Serializable {

    private final double totalActivityTime;
    private final double totalDistance;
    private final double totalAscent;

    public ActivitiesStats(double totalActivityTime, double totalDistance, double totalAscent){
        this.totalActivityTime = totalActivityTime;
        this.totalDistance = totalDistance;
        this.totalAscent = totalAscent;
    }

    public double getTotalActivityTime() {
        return totalActivityTime;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public double getTotalAscent() {
        return totalAscent;
    }
}
