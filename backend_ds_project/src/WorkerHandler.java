import shared_classes.ActivityResults;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class WorkerHandler extends Thread {

    /* contains shared queue for each open connection with a worker. This way Task Assigner will be able to send the gpx
    chunks to the appropriate worker using the shared queues.
     */
    private final ArrayList<Queue<GPXPacket>> workerQueues;
    private final HashMap<Integer, Queue<ActivityResults>> reduceThreadQueues;

    public WorkerHandler(ArrayList<Queue<GPXPacket>> workerQueues,
                         HashMap<Integer, Queue<ActivityResults>> reduceThreadQueues) {
        this.workerQueues = workerQueues;
        this.reduceThreadQueues = reduceThreadQueues;
    }

    @Override
    public void run(){

        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(5454);

            // Listen for connection requests from workers.
            while (true){
                Socket connectionSocket = serverSocket.accept();
                Queue<GPXPacket> sharedWorkQueue = new LinkedList<>(); // create shared queue for new worker connection.
                WorkerConnection workerConnection = new WorkerConnection(connectionSocket, sharedWorkQueue, reduceThreadQueues);
                workerConnection.start();

                synchronized (workerQueues){
                    workerQueues.add(sharedWorkQueue); // update list of shared work queues. Synchronization required.
                    workerQueues.notifyAll();
                }

            }

        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
}
