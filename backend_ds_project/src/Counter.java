public class Counter {
    private int count;

    public Counter(){
        this.count = 0;
    }

    public synchronized void increase(){
        count += 1;
    }

    public synchronized void decrease(){
        count -= 1;
    }

    public synchronized int getCount(){
        return count;
    }
}
