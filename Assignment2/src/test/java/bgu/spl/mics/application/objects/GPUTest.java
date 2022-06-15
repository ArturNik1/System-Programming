package bgu.spl.mics.application.objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GPUTest {

    private static GPU gpu1;
    private static GPU gpu2;
    private static GPU gpu3;
    private static GPU[] gpus;

    private static Data data;
    private static Model model;
    private static DataBatch batch;

    @Before
    public void setUp() throws Exception {
        gpu1 = new GPU(GPU.Type.RTX3090);
        gpu2 = new GPU(GPU.Type.RTX2080);
        gpu3 = new GPU(GPU.Type.GTX1080);
        gpus = new GPU[]{gpu1, gpu2, gpu3};

        data = new Data(Data.Type.Images, 2000);
        model = new Model("Model", data, new Student("a", "CS", Student.Degree.MSc));
    }

    @After
    public void tearDown() throws Exception {
    }

//    @Test
    public void setModel() {
        for (GPU gpu : gpus) {
            assertNull(gpu.getModel());
            assertEquals(0, gpu.getProcessedSize());
            assertEquals(0, gpu.getUnProcessedSize());
            gpu.setModel(model);
        }

        for (GPU gpu : gpus) {
            assertEquals(model, gpu.getModel());
            assertEquals(0, gpu.getProcessedSize());
            assertEquals(model.getData().getSize() / 1000, gpu.getUnProcessedSize());
        }

    }

//    @Test
    public void addToProcessedBatch() {
        for (GPU gpu : gpus) {
            batch = new DataBatch(data, 0, gpu);

            int sizeBefore = gpu.getProcessedSize();
            assertTrue(sizeBefore < gpu.getProcessedCapacity()); // check we can add the batch

            gpu.addToProcessedBatch(batch);

            int sizeAfter = gpu.getProcessedSize();
            assertEquals(sizeBefore + 1, sizeAfter);
            assertTrue(gpu.isProcessedContains(batch));
        }
    }

//    @Test
    public void tickTrain() {
        for (GPU gpu : gpus) {
            gpu.setModel(model);

            boolean finished = false;
            int startingTick = gpu.getStartingTick();
//            int tickTarget = gpu.getAmountOfTicksNeedToTrain();
            int tickTarget = 0;

            batch = new DataBatch(data, 0, gpu);
            gpu.addToProcessedBatch(batch);
            int processedAmount = gpu.getModel().getData().getProcessed();

            while (!finished) {
                gpu.tickTrain();
                boolean afterContains = gpu.isProcessedContains(batch);
                int tick = gpu.getTicks();
                if (tick - startingTick == tickTarget) {
                    // check if the batch is removed from the processed vector.
                    assertFalse(afterContains);

                    // check if processed += 1000
                    assertEquals(processedAmount + 1000, gpu.getModel().getData().getProcessed());

                    finished = true;
                }
                else {
                    assertTrue(afterContains);
                    assertEquals(processedAmount, gpu.getModel().getData().getProcessed());
                }
            }

        }
    }
}