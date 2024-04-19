import org.xml.sax.SAXException;
import shared_classes.ActivityResults;
import shared_classes.ActivitiesStats;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Queue;

public class UserConnection extends Thread{
    public final int  CONNECTION_ID;
    private final Socket connectionSocket;
    public final Queue<UserPacket> sharedMessageQueue;
    public final ActivityResults activityResults;


    // Used in order to synchronize between threads that respond to different request_codes of same user concurrently.
    private final HashMap<String, MultiReqSyncer> multiReqSyncers;
    private final ReadWriteLock multiReqLock;

    // In order to update/view usernames in thread-safe way.
    private final ReadWriteLock usernamesLock;

    public UserConnection(Integer connection_id, Socket connectionSocket, Queue<UserPacket> sharedMessageQueue,
                          ActivityResults activityResults, ReadWriteLock multiReqLock, ReadWriteLock usernamesLock, HashMap<String, MultiReqSyncer> multiReqSyncers){
        this.CONNECTION_ID = connection_id;
        this.connectionSocket = connectionSocket;
        this.sharedMessageQueue = sharedMessageQueue;
        this.activityResults = activityResults;
        this.multiReqSyncers = multiReqSyncers;
        this.multiReqLock = multiReqLock;
        this.usernamesLock = usernamesLock;
    }

    @Override
    public void run(){
        DataInputStream dis = null;
        ObjectOutputStream oos = null;
        StatExtractor statExtractor = new StatExtractor();
        StatLogger statLogger = new StatLogger();

        try {
            dis = new DataInputStream(connectionSocket.getInputStream());
            oos = new ObjectOutputStream(connectionSocket.getOutputStream());

            // Receive request_code.
            int request_code = dis.readInt();

            // If request_code == 0, receive GPX file and return activity results.
            if (request_code == 0) {

                // Receive gpx file from user.
                File gpxFile = receiveFile(dis);

                // Extract username from gpx file.
                String username = statExtractor.extractUsername(gpxFile);

                // Log username.
                usernamesLock.lockWrite();
                statLogger.logUsername(username);
                usernamesLock.unlockWrite();

                /* Create user packet that contains connection_id (in order to be able to send back the results to the
                corresponding user) along with the gpx file to send to task assigner. */
                UserPacket userPacket = new UserPacket(this.CONNECTION_ID, gpxFile);


                // Send user packet to task assigner.
                synchronized (sharedMessageQueue) {
                    sharedMessageQueue.add(userPacket);
                    sharedMessageQueue.notifyAll(); // Notify Task Assigner that a new message has been forwarded.
                }


                /* Wait for activity results (from the corresponding "reduce thread").
                Use variable activityResults for guarded suspension */

                synchronized (activityResults) {
                    // Use while loop check instead of 'if' statement to prevent spurious wake up of thread.
                    while (activityResults.isEmpty()) {
                        try {
                            activityResults.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    activityResults.setUsername(username);
                }

                // Send object.
                oos.writeObject(activityResults);

                // Once activity results have been received, update stats files.

                // Log user's route stats in xml file.
                statLogger.logRouteStats(username, activityResults);

                // Update user's total stats file.
                MultiReqSyncer syncer;

                multiReqLock.lockRead();
                if (!multiReqSyncers.containsKey(username)){
                    syncer = new MultiReqSyncer();
                    multiReqLock.unlockRead();
                    multiReqLock.lockWrite();
                    multiReqSyncers.put(username, syncer);
                    multiReqLock.unlockWrite();
                } else {
                    syncer = multiReqSyncers.get(username);
                    multiReqLock.unlockRead();
                }


                synchronized (syncer){
                    statLogger.updateUserTotalStats(username, activityResults);
                }

            }
            // If request_code == 1 send user total stats and global stats.
            else if (request_code == 1){

                // First get username of requester in order to parse appropriate file.
                String username = dis.readUTF();

                /* Request from user system will only be sent to server if at least one GPX file has been sent from user
                   so we can assume files and folders exist. */
                File userTotalStatsFile = new File(System.getProperty("user.dir") + "/user_total_stats/" + username + "_user_total_stats");

                multiReqLock.lockRead();
                MultiReqSyncer syncer = multiReqSyncers.get(username);
                multiReqLock.unlockRead();

                ActivitiesStats userStats;
                /* Two cases: 1) User sends request_code == 0 and after that request_code == 1, then synchronization
                   is required for thread-safe way to access total user stats file and because request_code == 0
                   was sent first, syncer exists.
                   2) User sends request_code == 1, then no synchronization is required, syncer may or may not exist.
                 */
                if (syncer != null){
                    synchronized (syncer){
                        userStats = statExtractor.extractStats(userTotalStatsFile);
                    }
                } else {
                    userStats = statExtractor.extractStats(userTotalStatsFile);
                }

                // Calculate global mean total stats.
                ActivitiesStats globalStats = calculateGlobalTotalMeanStats(multiReqSyncers, multiReqLock, usernamesLock);

                // Send objects.
                oos.writeObject(userStats);
                oos.writeObject(globalStats);
            }

        } catch (IOException | ParserConfigurationException | TransformerException | SAXException |
                 InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                dis.close();
                oos.close();
                connectionSocket.close(); // Terminate user connection.
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }

        }

    }


    private File receiveFile(DataInputStream dis) throws IOException{
        long fileSize = dis.readLong(); // read file size.
        String fileName = dis.readUTF(); // read file name.

        String newFileName = "temp_" + fileName;
        String filePath = System.getProperty("user.dir") + "\\" + newFileName;

        File tempFile = new File(filePath);
        FileOutputStream output = new FileOutputStream(tempFile);

        int read = 0;
        while (read < fileSize){
            output.write(dis.readByte());
            read++;
        }

        output.close();
        return tempFile;
    }

    private ActivitiesStats calculateGlobalTotalMeanStats(HashMap<String, MultiReqSyncer> multiReqSyncers,
                                                          ReadWriteLock multiReqLock, ReadWriteLock usernamesLock) throws InterruptedException {
        double meanTotalDistance = 0;
        double meanTotalActivityTime = 0;
        double meanTotalAscent = 0;
        StatExtractor statExtractor = new StatExtractor();

        // Get usernames. (Need to be stored otherwise only active session's user total stats will be counted)
        usernamesLock.lockRead();
        HashSet<String> usernames = statExtractor.extractUsernames();
        usernamesLock.unlockRead();

        for (String username : usernames){

            multiReqLock.lockRead();
            MultiReqSyncer syncer = multiReqSyncers.get(username);
            multiReqLock.unlockRead();

            File userTotalStatsFile = new File(System.getProperty("user.dir") + "/user_total_stats/" + username + "_user_total_stats");
            ActivitiesStats userTotalStats;

            /* Two cases: 1) User sends request_code == 0 and after that request_code == 1, then synchronization
                   is required for thread-safe way to access total user stats file and because request_code == 0
                   was sent first, syncer exists.
                   2) User sends request_code == 1, then no synchronization is required, syncer may or may not exist.
                 */
            if (syncer != null){
                synchronized (syncer){
                    userTotalStats = statExtractor.extractStats(userTotalStatsFile);
                }
            } else {
                userTotalStats = statExtractor.extractStats(userTotalStatsFile);
            }

            meanTotalDistance += userTotalStats.getTotalDistance();
            meanTotalAscent += userTotalStats.getTotalAscent();
            meanTotalActivityTime += userTotalStats.getTotalActivityTime();
        }
        int numberOfUsers = usernames.size();

        return new ActivitiesStats(meanTotalActivityTime/numberOfUsers, meanTotalDistance/numberOfUsers,
                meanTotalAscent/numberOfUsers);
    }
}












