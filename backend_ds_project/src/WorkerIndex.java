public class WorkerIndex {
    private int index;
    public WorkerIndex(){
        this.index = -1;
    }

    public int getNext(int numOfWorkers){
        index = (index + 1) % numOfWorkers;
        return index;
    }
}
