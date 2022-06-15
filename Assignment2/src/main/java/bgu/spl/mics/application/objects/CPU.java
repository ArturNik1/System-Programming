package bgu.spl.mics.application.objects;
import bgu.spl.mics.Callback;

import java.util.Stack;
/**
 * Passive object representing a single CPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class CPU {
    private int cores;
    private Stack<DataBatch> data;
    private Cluster cluster;
    private Statistics statistics;
    private int startingTick;
    private int reqTicks;
    private int ticks;
    private Callback ter;
    public CPU(int cores,Cluster cluster)
    {
        this.cores=cores;
        this.cluster=cluster;
        this.statistics = cluster.getStatistics();
        data=new Stack<DataBatch>();
        this.ticks=0;
        this.startingTick=0;
        this.reqTicks=0;

    }

    /**
     * work on the Data batch , send it to cluster if finish
     * @post: Data batch progress=@pre(Data batch progress)+1
     */
    public void tickProcess() {
        // update ticks field
        // check if we have finished processing the batch
        // 1. if we did - send it back and request a new one.
        // 2. if we didn't - request a new one if data is empty.
        if (!data.isEmpty()) {
            ticks++;
            statistics.incCPUTimeUnits();
            if (ticks - startingTick >= reqTicks) {
                finishProcess();
                getNewData();
            }
        }
        else {
            getNewData();
        }

    }

    /**
     * get new batch from Cluster
     * @pre: isInProcess==true
     *
     */
    private void getNewData() {

        DataBatch batch = cluster.sendToCPU();
        if (batch.getGpu() == null) {
            ter.call(new Object());
            return; // shouldn't happen (unless the thread was interrupted)
        }
        reqTicks = calculateReqTicks(batch.getData().getType());
        data.push(batch);
        startingTick = ticks; // start counting from here...
    }

    /**
     * return Batch to Cluster after process
     * @pre: this.getTimeLeftToProcess()=1
     * @post: Processed Batch moved to Cluster
     */
    private void finishProcess() {
        statistics.incTotalBatches();
        cluster.moveFromCPU(data.pop());
    }

    private int calculateReqTicks(Data.Type type) {
        if (type == Data.Type.Images) return (32 / cores) * 4;
        else if (type == Data.Type.Text) return (32 / cores) * 2;
        else return (32 / cores);
    }

    public boolean isInProcess() {
        return !data.isEmpty();
    }

    /**
     *
     * @return timer that left to process
     */
    public int getTimeLeftToProcess() {
        return (ticks - startingTick - reqTicks);
    }

    public int getCores() {
        return cores;
    }

    public void setTer(Callback ter) {
        this.ter = ter;
    }
    public void addToData(DataBatch batch) {
        data.push(batch);
    }
}
