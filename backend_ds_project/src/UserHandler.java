import shared_classes.ActivityResults;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Queue;

public class UserHandler extends Thread{
    private final Queue<UserPacket> sharedMessageQueue;
    private final HashMap<Integer, ActivityResults> userConnections;

    public UserHandler(Queue<UserPacket> sharedMessageQueue, HashMap<Integer, ActivityResults> userConnections){
        this.sharedMessageQueue = sharedMessageQueue;
        this.userConnections = userConnections;
    }

    @Override
    public void run(){
        ServerSocket serverSocket = null;

        // Used in order to synchronize between threads that respond to different request_codes of same user concurrently.
        HashMap<String, MultiReqSyncer> multiReqSyncers = new HashMap<>();
        ReadWriteLock multiReqLock = new ReadWriteLock();

        // In order to update/view usernames in thread-safe way.
        ReadWriteLock usernamesLock = new ReadWriteLock();

        try {
            serverSocket = new ServerSocket(6969);
            int connectionCount = 0; // Used to assign CONNECTION_ID to each "User connection thread"

            // Listen for connection requests from users.
            while (true){
                Socket connectionSocket = serverSocket.accept();
                connectionCount += 1;


                // Create shared object in order for results to be passed from "reduce thread" to user connection.
                ActivityResults activityResults = new ActivityResults();

                // Create new thread for new connection socket (user).
                UserConnection userConnection = new UserConnection(connectionCount, connectionSocket,
                        sharedMessageQueue, activityResults, multiReqLock, usernamesLock, multiReqSyncers);

               // List connection in order for task assigner's "reduce threads" to be able to send results to users.
                this.userConnections.put(userConnection.CONNECTION_ID, activityResults); // NO need for synchronization since Task Assigner only reads from hashmap.
                userConnection.start();

            }


        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }
}
