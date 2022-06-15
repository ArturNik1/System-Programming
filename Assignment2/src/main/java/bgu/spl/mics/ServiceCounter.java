package bgu.spl.mics;

public class ServiceCounter {

    private int serviceCounter;
    private int targetCounter;
    private int targetGPU;
    private ServiceCounter() {
        this.serviceCounter = 0;
    }

    public int getTargetGPU() {
        return targetGPU;
    }

    public void setTargetGPU(int targetGPU) {
        this.targetGPU = targetGPU;
    }

    private static class ServiceCounterHolder {
        private static ServiceCounter instance = new ServiceCounter();
    }

    public static ServiceCounter getInstance() {
        return ServiceCounterHolder.instance;
    }

    public synchronized int getServiceCounter() {
        return this.serviceCounter;
    }

    public int getTargetCounter() {
        return this.targetCounter;
    }

    public synchronized void incServiceCounter() {
        this.serviceCounter++;
    }

    public synchronized void setTargetCounter(int counter) {
        this.targetCounter = counter;
    }

}
