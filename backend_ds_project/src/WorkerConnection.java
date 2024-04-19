import shared_classes.ActivityResults;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Queue;

public class WorkerConnection extends Thread {
    public final Socket connectionSocket;
    private final Queue<GPXPacket> sharedWorkQueue;
    private final HashMap<Integer, Queue<ActivityResults>> reduceThreadQueues;

    public WorkerConnection(Socket connectionSocket, Queue<GPXPacket> sharedWorkQueue,
                            HashMap<Integer, Queue<ActivityResults>> reduceThreadQueues) {
        this.connectionSocket = connectionSocket;
        this.sharedWorkQueue = sharedWorkQueue;
        this.reduceThreadQueues = reduceThreadQueues;
    }

    @Override
    public void run() {

        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try {
            ois = new ObjectInputStream(connectionSocket.getInputStream());
            oos = new ObjectOutputStream(connectionSocket.getOutputStream());

            ObjectOutputStream finalOos = oos;

            // This thread sends the gpx chunks to the worker.
            Thread outputStreamThread = new Thread(() ->
            {

                while (true) {
                    synchronized (sharedWorkQueue) {
                        if (!sharedWorkQueue.isEmpty()) {
                            GPXPacket gpxPacket = sharedWorkQueue.remove();
                            try {
                                finalOos.writeObject(gpxPacket);
                                finalOos.flush();// send gpx packet to worker.
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                sharedWorkQueue.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            outputStreamThread.start();


            /* This thread receives intermediate results from the worker and forwards them to the correct "reduce thread"
            by accessing the hashmap<Integer, sharedQueue>*/
            while (true) {

                try {
                    // receive packet that contains intermediate results.
                    WorkerPacket workerPacket = (WorkerPacket) ois.readObject();
                    synchronized (reduceThreadQueues){
                        synchronized (reduceThreadQueues.get(workerPacket.CONNECTION_ID)) {
                            reduceThreadQueues.get(workerPacket.CONNECTION_ID).add(workerPacket.getIntermediateResults());
                            reduceThreadQueues.get(workerPacket.CONNECTION_ID).notifyAll();
                        }
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                oos.close();
                ois.close();
                connectionSocket.close(); // Terminate worker connnection.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
