package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateTickBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.CPU;
import bgu.spl.mics.application.objects.Cluster;

/**
 * This class may not hold references for objects which it is not responsible for.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class CPUService extends MicroService {

    private CPU cpu;

    public CPUService(String name,CPU cpu) {
        super(name);
        this.cpu = cpu;
    }

    @Override
    protected void initialize() {
        subscribeBroadcast(TerminateTickBroadcast.class, broadcast -> {

            this.terminate();
            Cluster.getInstance().addFinished();
        });

        subscribeBroadcast(TickBroadcast.class, broadcast -> {
            cpu.tickProcess();
        });
        cpu.setTer((x)->{terminate();
        Cluster.getInstance().addFinished();});
    }
}
