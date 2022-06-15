package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.PublishConfrenceBroadcast;
import bgu.spl.mics.application.messages.PublishResultsEvent;
import bgu.spl.mics.application.messages.TerminateTickBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Cluster;
import bgu.spl.mics.application.objects.ConfrenceInformation;

/**
 * Conference service is in charge of
 * aggregating good results and publishing them via the {@link PublishConfrenceBroadcast},
 * after publishing results the conference will unregister from the system.
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class ConferenceService extends MicroService {

    private ConfrenceInformation conferenceInfo;

    public ConferenceService(String name, ConfrenceInformation conferenceInfo) {
        super(name);
        this.conferenceInfo = conferenceInfo;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminateTickBroadcast.class, broadcast -> {
            Cluster.getInstance().addFinished();
            this.terminate();

        });

        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            // check if conference is due.
            conferenceInfo.incTicks();
            if (conferenceInfo.isDone()) {
                System.out.println(getName()+"is Done and publish now");
                sendBroadcast(new PublishConfrenceBroadcast(conferenceInfo.getModelNames()));
                System.out.println( this.getName()+"published");
                Cluster.getInstance().addFinished();
                this.terminate();
            }
        });

        subscribeEvent(PublishResultsEvent.class, event -> {
            System.out.println("send "+event.getModel().getName()+" to conference: "+this.getName());
            conferenceInfo.addToList(event.getModel().getName());
            conferenceInfo.addToModelList(event.getModel());
        });

    }
}
