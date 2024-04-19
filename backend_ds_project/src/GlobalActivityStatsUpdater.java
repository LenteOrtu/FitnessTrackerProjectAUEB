import shared_classes.ActivityResults;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;

public class GlobalActivityStatsUpdater extends Thread{

    private final ReadWriteLock lock;
    private final Queue<ActivityResults> sharedResultsQueue;

    public GlobalActivityStatsUpdater(ReadWriteLock lock, Queue<ActivityResults> sharedResultsQueue){
        this.lock = lock;
        this.sharedResultsQueue = sharedResultsQueue;
    }

    @Override
    public void run(){
        try{
            // Create global activity stats file if it does not exist and write default values.
            File globalActivityStatsFile = new File(System.getProperty("user.dir") + "\\global_stats");
            if (!globalActivityStatsFile.exists()) {
                globalActivityStatsFile.createNewFile();
                PrintWriter pw = new PrintWriter(globalActivityStatsFile);
                pw.println("0,0,0,0,NULL");
                pw.close();
            }

        }catch (IOException e){
            e.printStackTrace();
        }

        StatLogger statLogger = new StatLogger();

        // Update global activity stats file every X milliseconds.
        while (true){
            synchronized (sharedResultsQueue){
                if (!sharedResultsQueue.isEmpty()){
                    Queue<ActivityResults> activityResultsQueue = new LinkedList<>();
                    while (!sharedResultsQueue.isEmpty()){
                        activityResultsQueue.add(sharedResultsQueue.remove());
                    }
                    try{
                        lock.lockWrite();
                        statLogger.updateGlobalTotalStats(activityResultsQueue);
                        lock.unlockWrite();
                    }catch (IOException | InterruptedException e){
                        e.printStackTrace();
                    }

                }else{
                    try {
                        sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}







