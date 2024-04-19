// Used in order to update the users_stats file in a thread safe way.
public class ReadWriteLock {
    private int readers;
    private boolean writer;

    public synchronized void lockRead() throws InterruptedException{
        while (writer){
            wait();
        }
        readers++;
    }

    public synchronized void unlockRead() {
        readers--;
        notifyAll();
    }

    public synchronized void lockWrite() throws InterruptedException {
        while (writer || readers > 0) {
            wait();
        }
        writer = true;
    }

    public synchronized void unlockWrite(){
        writer = false;
        notifyAll();
    }
}
