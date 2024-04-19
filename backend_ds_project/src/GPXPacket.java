import java.io.Serializable;
import java.util.ArrayList;

public class GPXPacket implements Serializable {
    /* A serializable object that contains CONNECTION_ID so intermediate results can be returned back to correct "reduce thread" and therefore user.
    (CONNECTION_ID works as key for map and reduce functions)
    Also contains a linked list of size chunkSize that contains gpx point objects.
    */

    public final int CONNECTION_ID;
    private final ArrayList<GPXPoint> gpxPoints;

    public GPXPacket(int CONNECTION_ID, ArrayList<GPXPoint> gpxPoints){
        this.CONNECTION_ID = CONNECTION_ID;
        this.gpxPoints = gpxPoints;
    }

    public ArrayList<GPXPoint> getGpxPoints(){
        return this.gpxPoints;
    }
}
