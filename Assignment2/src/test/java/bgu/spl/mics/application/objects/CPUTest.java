package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class CPUTest {
    private static CPU cpu;
    private static GPU gpu;
    private static Cluster cluster;
    private static Data data;
    private static DataBatch dataBatch;
    private static DataBatch dataBatch2;
    @Before
    public void setUp() throws Exception {
        cluster=Cluster.getInstance();
        cpu=new CPU(8,cluster);
        gpu=new GPU(GPU.Type.RTX3090);
        data=new Data(Data.Type.Images,20);
        dataBatch=new DataBatch(data,0,gpu);
        dataBatch2=new DataBatch(data,10,gpu);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void tickProcess() {
//        cpu.tickProcess();
        assertFalse(cpu.isInProcess());
        cluster.moveFromGPU(dataBatch);
        cluster.moveFromGPU(dataBatch2);
        int timeLeft= cpu.getTimeLeftToProcess();
        while(timeLeft>1)
        {
//            cpu.tickProcess();
            assertEquals(cpu.getTimeLeftToProcess(),timeLeft-1);
            timeLeft--;
        }
        if(cpu.getTimeLeftToProcess()==1)
        {
//            cpu.tickProcess();
            assertTrue(gpu.isProcessedContains(dataBatch));
            assertTrue(cpu.getTimeLeftToProcess()==(32/cpu.getCores())*4);
        }


    }
}