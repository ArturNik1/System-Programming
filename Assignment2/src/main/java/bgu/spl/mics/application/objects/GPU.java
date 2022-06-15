package bgu.spl.mics.application.objects;

import bgu.spl.mics.Event;
import bgu.spl.mics.application.messages.TrainModelEvent;
import bgu.spl.mics.application.services.GPUService;

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Passive object representing a single GPU.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class GPU {
    /**
     * Enum representing the type of the GPU.
     */
    enum Type {RTX3090, RTX2080, GTX1080}

    private Type type;
    private Model model;
    private Cluster cluster;
    private Statistics statistics;
    private Vector<DataBatch> unprocessed;
    private BlockingQueue<DataBatch> processed;
    private int available;
    private int startingTick;
    private int ticks;
    private int capacity;

    private int trainedBatches;
    private int totalBatches;
    private int amountOfTicksNeedToTrain;

    private TrainModelEvent trainEvent;
    private GPUService service;

    public GPU(Type type)
    {
        this.type=type;
        this.model=null;
        this.cluster = Cluster.getInstance();
        this.statistics = cluster.getStatistics();
        this.unprocessed=new Vector<>();
        setProcessedSize();
        this.startingTick=0;
        this.ticks=0;

        this.trainedBatches = 0;
        this.totalBatches = 0;
        setAmountOfTicksNeededToTrain();

        this.trainEvent = null;
        this.service = null;
    }

    public void setService(GPUService service) {
        this.service = service;
    }

    /**
     * @param model
     * @post this.model == model
     * @post this.processed.getProcessedSize() == 0
     * @post this.unprocessed.getUnProcessedSize() > 0
     */
    public void setModel(Model model) {
        this.model = model;
        clearProcessed(); // clears the processed vector
        divideToBatches(); // fills the unprocessed vector

        model.setStatus(Model.Status.Training);
        startingTick = ticks;
    }

    public void setModelTest(Model model) {
        this.model = model;
    }

    /**
     * adding the batch to processed.
     * @param batch
     * @pre isProcessedContains(batch) == false
     * @post isProcessedContains(batch) == true
     */
    public void addToProcessedBatch(DataBatch batch) {
        // adds the batch to the processed vector
        // *** available will always be < capacity
        if (processed.size() == 0) startingTick = ticks;
        processed.add(batch);
        model.getData().incProcessed();
    }

    /**
     * updates the ticks field and checks whether we have finished training a batch.
     * @post this.ticks == @pre(this.ticks+1)
     */
    public void tickTrain() {
        // updates the ticks field
        // checks if we've finished training a batch and calls finishTrainWithBatch()
        // otherwise, waits for more ticks.
        if (trainEvent == null) return;

        ticks++;
        //statistics.incGPUTimeUnits();
        if (ticks - startingTick == amountOfTicksNeedToTrain) {
            // finished training the batch
           // System.out.println("tick: "+ticks+" starting tick: "+startingTick + " amount needed: " + amountOfTicksNeedToTrain + " size: " + processed.size());
          //  System.out.println(trainEvent.getModel().getName()+" Trained Batches: " + trainedBatches + " Total Batches: " + totalBatches);
            finishTrainWithBatch();
            startingTick = ticks;
        }
        // what happens when there are no more batches to train?
        // 1. there are no unprocessed/processed batches.
        // - gpu is done training this model. use complete with the message bus
        if (isDoneTraining()) {
           // System.out.println("finish training: "+trainEvent.getModel().getName()+" Trained Batches: " + trainedBatches + " Total Batches: " + totalBatches);
           // System.out.println("Process list size: "+ processed.size()+ " unprocessed list size: "+unprocessed.size());
            model.setStatus(Model.Status.Trained);
          //  System.out.println(trainEvent.toString());/////-------
            service.finishTraining(trainEvent);

        }
    }

    /**
     * init the processed with a cap.
     * @post this.processed != null
     */
    private void setProcessedSize() {
        // set the processed vector (with size)
        int amount = 8;
        if (type == Type.RTX3090) amount = 32;
        else if (type == Type.RTX2080) amount = 16;
        processed = new LinkedBlockingQueue<>();
        available = amount;
        capacity=amount;
    }

    /**
     * divides the data into batches of 1000's.
     * @pre this.getUnProcessedSize() == 0
     */
    private void divideToBatches() {
        // gets the data from the Model and divides it accordingly to the unprocessed vector.
        Data data = model.getData();
        System.out.println(trainEvent.getModel().getName()+"- Printing data size: " + data.getSize() + " in model: " + model.getName() + " : " + unprocessed.size());
        for (int i = 0; i < data.getSize(); i += 1000) {
            unprocessed.add(new DataBatch(data, i, this));
        }
        totalBatches = unprocessed.size();
        System.out.println(trainEvent.getModel().getName()+"- Num of Data total baches: "+totalBatches);
        trainedBatches = 0;
    }

    /**
     * moves an amount of unprocessed batches to the Cluster.
     * @pre this.getUnProcessedSize() != 0
     * @post this.getUnProcessedSize() < @pre(this.getUnProcessedSize())
     */
    public void moveToProcessCPU() { // TODO maybe add tests? (changed from private to public)
        // moves X amount of unprocessed batches to the Cluster.
        // removes those DataBatches from the unprocessed vector.
       // System.out.println(this.getModel().getName()+" send to cluster");
        int i;
        for (i = 0; i < available && !unprocessed.isEmpty(); i++) {
            cluster.moveFromGPU(unprocessed.remove(0));
        }
        available -= i; // we've sent all the available DataBatches to the cluster
    }

    /**
     * removing the batch from the processed vector.
     * @pre this.getProcessedSize() > 0
     * @post this.getProcessedSize() == @pre(this.getProcessedSize() - 1)
     */
    private void finishTrainWithBatch() {
        // removes the batch from the processed vector.
        try {
            if (!processed.isEmpty()) {
                processed.take();
                available++;
                trainedBatches++;
                statistics.aa(amountOfTicksNeedToTrain);
               // System.out.println("finish train batch: "+trainEvent.getModel().getName());
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted!");
        }
        moveToProcessCPU();
    }

    /**
     * clears processed vector
     * @post this.getProcessedSize() == 0
     */
    private void clearProcessed() {
        //clear vector processed
        processed.clear();
        available = capacity;
    }

    // getters

    private boolean isDoneTraining() {
        return (trainedBatches == totalBatches && totalBatches != 0);
    }

    public void setEvent(TrainModelEvent e) {
        this.trainEvent = e;
    }

    /**
     * @return this.model
     */
    public Model getModel() { return this.model; }

    public Event getEvent() { return this.trainEvent; }

    /**
     * @return the amount of elements in this.processed.
     */
    public int getProcessedSize() {
        return processed.size();
    }

    /**
     * @return the capacity of this.processed.
     */
    public int getProcessedCapacity() {
        return capacity;
    }

    /**
     * @return the amount of element in this.unprocessed.
     */
    public int getUnProcessedSize() {
        return unprocessed.size();
    }

    /**
     * checks if the batch is in this.processed.
     * @param batch
     * @return true if it's in, false if it's not.
     */
    public boolean isProcessedContains(DataBatch batch) {
        return processed.contains(batch);
    }

    public int getStartingTick() { return startingTick; }

    public int getTicks() { return ticks; }

    /**
     * checks how many ticks do we need for this batch.
     * @return the amount of ticks needed to finish processing.
     */
    private void setAmountOfTicksNeededToTrain() {
        if (type == Type.RTX3090) amountOfTicksNeedToTrain = 1;
        else if (type == Type.RTX2080) amountOfTicksNeedToTrain = 2;
        else amountOfTicksNeedToTrain = 4;
    }

    public int getAmountOfTicksNeedToTrain() {
        return this.amountOfTicksNeedToTrain;
    }
    public void terCpu()
    {
        if(cluster.addFinishedGPU()) {
            System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXX");
            for (int i = 0; i < cluster.getTarget(); i++)
                cluster.moveFromGPU(new DataBatch(null, 0, null));
        }
    }
}
