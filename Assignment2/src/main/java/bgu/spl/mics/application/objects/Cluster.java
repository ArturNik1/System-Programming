package bgu.spl.mics.application.objects;


import bgu.spl.mics.ServiceCounter;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Passive object representing the cluster.
 * <p>
 * This class must be implemented safely as a thread-safe singleton.
 * Add all the fields described in the assignment as private fields.
 * Add fields and methods to this class as you see fit (including public methods and constructors).
 */
public class Cluster {

	private LinkedList<GPU> gpus; // TODO add gpus/cpus to here
	private LinkedList<CPU> cpus;
	private BlockingQueue<DataBatch> unprocessed ;
	private Statistics statistics;
	private AtomicInteger finished;
	private int target;
	private Object unprocessedKey = new Object();
	private int targetGpu;
	private AtomicInteger gpuFinish;


	private static class ClusterHolder {
		private static Cluster instance = new Cluster();
	}
	private Cluster()
	{
		unprocessed= new LinkedBlockingQueue<>();
		statistics = new Statistics();
		this.finished=new AtomicInteger(0);
		target = ServiceCounter.getInstance().getTargetCounter();
		targetGpu=ServiceCounter.getInstance().getTargetGPU();
		this.gpuFinish=new AtomicInteger(0);
	}

	/**
     * Retrieves the single instance of this class.
     */
	public static Cluster getInstance() {
		return ClusterHolder.instance;
	}

	public Statistics getStatistics() {
		return this.statistics;
	}

	public void setGPUS(LinkedList<GPU> gpus) {
		this.gpus = gpus;
	} // TODO must set this

	public void setCPUS(LinkedList<CPU> cpus) {
		this.cpus = cpus;
	}

	public synchronized void moveFromCPU(DataBatch dataBatch)
	{
		//insert processed batch to cluster

		GPU gpu = dataBatch.getGpu();
		gpu.addToProcessedBatch(dataBatch);
	}
	public void moveFromGPU(DataBatch dataBatch)
	{

		try {
			unprocessed.put(dataBatch);
		} catch (InterruptedException e) {
			System.out.println("Interrupted!");
		}

	}

	public DataBatch sendToCPU() {
		try {


			DataBatch batch = unprocessed.take();
			//System.out.println("taken");
			return batch;
		} catch (InterruptedException e) {
			System.out.print("Interrupted!");
			return null;
		}
	}
	public synchronized void  addFinished() {
		this.finished.incrementAndGet();
		System.out.println("Finished :"+finished.get()+ "  Target:"+target);
		if(finished.get()==target)
			Cluster.getInstance().statistics.generateOutputFile();
	}
	public synchronized boolean  addFinishedGPU() {
		this.gpuFinish.incrementAndGet();
		if(gpuFinish.get()==targetGpu)
			return true;
		return false;
	}
	public int getTarget() {
		return target;
	}
}
