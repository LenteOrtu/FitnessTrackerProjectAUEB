import shared_classes.ActivityResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class TaskAssigner extends Thread{
    public int SENDER_LIMIT = 5; // Max amount of different gpx file's chunks sent at the same time.
    public final Queue<UserPacket> sharedMessageQueue;
    public final HashMap<Integer, ActivityResults> userConnections;
    public final ArrayList<Queue<GPXPacket>> workerQueues;

    // Contains shared queue for each thread that reduces the intermediate results for a specific user.
    public final HashMap<Integer, Queue<ActivityResults>> reduceThreadQueues;
    private final ReadWriteLock lock;

    public TaskAssigner(Queue<UserPacket> sharedMessageQueue, HashMap<Integer, ActivityResults> userConnections,
                        ArrayList<Queue<GPXPacket>> workerQueues, HashMap<Integer, Queue<ActivityResults>> reduceThreadQueues,
    ReadWriteLock lock){
        this.sharedMessageQueue = sharedMessageQueue;
        this.userConnections = userConnections;
        this.workerQueues = workerQueues;
        this.reduceThreadQueues = reduceThreadQueues;
        this.lock = lock;
    }

    @Override
    public void run(){
        /* Create shared results queue so that the global stats updater thread can receive results from the reduce
        threads. */
        Queue<ActivityResults> sharedResultsQueue = new LinkedList<>();

        // Create global activity stats updater thread that updates the global_activity_stats file every ~x seconds.
        GlobalActivityStatsUpdater gasu = new GlobalActivityStatsUpdater(lock, sharedResultsQueue);
        gasu.start();

        WorkerIndex workerIndex = new WorkerIndex(); // Used for reduce threads to synchronize and send chunks in Round Robin fashion.
        Counter activeSenders = new Counter(); // Used to limit the number of threads allowed to send gpx chunks at the same time.

        // Constantly check shared queue for messages.
        while (true){
            synchronized (sharedMessageQueue){
                if (!sharedMessageQueue.isEmpty()){

                    if (activeSenders.getCount() < SENDER_LIMIT) {
                        UserPacket userPacket = sharedMessageQueue.remove();


                        Queue<ActivityResults> intermediateResultsQueue = new LinkedList<>();
                        synchronized (reduceThreadQueues){
                            reduceThreadQueues.put(userPacket.CONNECTION_ID, intermediateResultsQueue);
                        }

                        /* Parses GPX File and sends gpx chunks in round robin fashion, then waits for intermediate results
                        from workers, then reduces them and sends the final activity results to the appropriate user connection. */
                        ResultReducer reduceThread = new ResultReducer(workerIndex, activeSenders, userConnections.get(userPacket.CONNECTION_ID),
                                intermediateResultsQueue, sharedResultsQueue, workerQueues, userPacket);
                        activeSenders.increase();
                        reduceThread.start();
                    }

                } else {
                    try {
                        sharedMessageQueue.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}






