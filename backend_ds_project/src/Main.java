import shared_classes.ActivityResults;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

/* Assertion: Each different user (key=username) can send only one GPX file at a time and cannot send more until his
request has been completed */
public class Main {


    public static void main(String[] args) throws IOException {
        HashMap<Integer, ActivityResults> userConnections = new HashMap<>();
        Queue<UserPacket> sharedMessageQueue = new LinkedList<>();
        ArrayList<Queue<GPXPacket>> workerQueues = new ArrayList<>();
        HashMap<Integer, Queue<ActivityResults>> reduceThreadQueues = new HashMap<>();
        ReadWriteLock lock = new ReadWriteLock(); // Used to update global stats file in thread safe way.

        WorkerHandler workerHandler = new WorkerHandler(workerQueues, reduceThreadQueues);
        UserHandler userHandler = new UserHandler(sharedMessageQueue, userConnections);
        TaskAssigner taskAssigner = new TaskAssigner(sharedMessageQueue, userConnections, workerQueues, reduceThreadQueues, lock);

        workerHandler.start();
        userHandler.start();
        taskAssigner.start();
    }
}
