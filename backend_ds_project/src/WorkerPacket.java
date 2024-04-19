import shared_classes.ActivityResults;

import java.io.Serializable;

public class WorkerPacket implements Serializable {

    public final int CONNECTION_ID;
    private final ActivityResults intermediateResults;

    public WorkerPacket(int CONNECTION_ID, ActivityResults intermediateResults){
        this.CONNECTION_ID = CONNECTION_ID;
        this.intermediateResults = intermediateResults;
    }

    public ActivityResults getIntermediateResults(){
        return this.intermediateResults;
    }

}
