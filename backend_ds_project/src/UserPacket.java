import java.io.File;
import java.io.Serializable;

public class UserPacket implements Serializable {
    /* This packet is forwarded from a UserConnection thread to the Task Assigner thread. The packet contains
    the GPX file that the user sent along with a shared shared_classes.ActivityResults object that will be used to pass the
    user's activity results from the Task Assigner's thread to the user via the User Connection Thread's output stream.
     */

    public final int CONNECTION_ID;
    private final File gpxFile;

    public UserPacket(int CONNECTION_ID, File gpxFile){
        this.CONNECTION_ID = CONNECTION_ID;
        this.gpxFile = gpxFile;
    }

    public File getGpxFile() {
        return gpxFile;
    }

}
