package bgu.spl.mics.application.services;

import bgu.spl.mics.Event;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.GPU;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * GPU service is responsible for handling the
 * {@link TrainModelEvent} and {@link TestModelEvent},
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class GPUService extends MicroService {
    private GPU gpu;
    private Queue<TrainModelEvent> trainStandby;

    public GPUService(String name, GPU gpu) {
        super(name);
        this.gpu = gpu;
        this.trainStandby = new LinkedList<>();
    }

    public void finishTraining(Event e) {
        complete(e, gpu.getModel());
        if (!trainStandby.isEmpty()) {
            TrainModelEvent event = trainStandby.remove();
            gpu.setEvent(event);
            gpu.setModel(event.getModel());
            gpu.moveToProcessCPU();
        }else gpu.setEvent(null);
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminateTickBroadcast.class, broadcast -> {
            if(gpu.getEvent()!=null)complete(gpu.getEvent(), null);
            for(TrainModelEvent e:trainStandby)complete(e, null);
            gpu.terCpu();
            this.terminate();
            Cluster.getInstance().addFinished();
        });

        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            // what should the gpuservice do when it gets a tick
            gpu.tickTrain();
        });

        subscribeEvent(TrainModelEvent.class, event -> {
            // check if event is being processed
            // 1.   if it is being processed - move the callback to a different queue for us to deal with it later.
            //      we will deal with them whenever we finish training/testing a model.
            // 2.   otherwise, just run the function and do not add the callback to the second queue.
            if (gpu.getEvent() == null) {
                gpu.setEvent(event);
                gpu.setModel(event.getModel());
                gpu.moveToProcessCPU();
            }
            else {
                System.out.println("StandBy "+event.getModel().getName());
                System.out.println("Cause to StandBy: "+gpu.getModel().getName());
                trainStandby.add(event);
            }
        });

        subscribeEvent(TestModelEvent.class, event -> {
            Model model = event.getModel();
            model.changeStatusToTested();
            double num = Math.random();
            if (model.getStudent().isMSc()) {
                // msc
                if (num <= 0.6) model.changeResult("Good");
                else model.changeResult("Bad");
            }
            else {
                // phd
                if (num <= 0.8) model.changeResult("Good");
                else model.changeResult("Bad");
            }
            complete(event, model);
        });
    }

}
