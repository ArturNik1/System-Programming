package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.*;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.Model;
import bgu.spl.mics.application.objects.Student;

import java.util.HashSet;
import java.util.LinkedList;

/**
 * Student is responsible for sending the {@link TrainModelEvent},
 * {@link TestModelEvent} and {@link PublishResultsEvent}.
 * In addition, it must sign up for the conference publication broadcasts.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */

public class StudentService extends MicroService {
    private Student student;

    private int currentModelIndex;
    private Model currentModel;
    private Future<Model> trainFuture;
    private Future<Model> testFuture;

    public StudentService(String name, Student student) {
        super(name);
        this.student = student;
        this.currentModelIndex = 0;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminateTickBroadcast.class, broadcast -> {

            this.terminate();
            Cluster.getInstance().addFinished();
        });

        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            // check if we are training a model.
            // if not -> send the event to train the model.
            // otherwise -> wait for it to complete. afterwards, send the testModelEvent and wait for it to complete.
            if (student.getModels() != null && currentModelIndex < student.getModels().length
                    && !student.isTraining(currentModelIndex)) {
                System.out.println(getName()+" train new model "+student.getCurrentModel(currentModelIndex).getName());
                trainFuture = sendEvent(new TrainModelEvent(student.getCurrentModel(currentModelIndex)));
                currentModelIndex++;
                System.out.println(getName()+" want to get");
                currentModel = trainFuture.get();
                if(currentModel==null)
                {
                    currentModelIndex=student.getModels().length;
                    return;
                }
                System.out.println(getName()+" send to test");
                testFuture = sendEvent(new TestModelEvent(currentModel));
                System.out.println(getName()+" wait to test");
                currentModel = testFuture.get();
                System.out.println(currentModel.getResult().name());
                if (currentModel.getResult() == Model.Results.Good) {
                    System.out.println(getName()+" sent to publish");
                    student.incPublications();
                    sendEvent(new PublishResultsEvent(currentModel));
                }
            }
        });

        subscribeBroadcast(PublishConfrenceBroadcast.class, broadcast -> {
            HashSet<String> names = broadcast.getNames();
            System.out.println("read");
            student.incPapersRead(names);

        });

    }
}
