package bgu.spl.mics.application.services;

import bgu.spl.mics.Broadcast;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.messages.TerminateTickBroadcast;
import bgu.spl.mics.application.messages.TickBroadcast;
import bgu.spl.mics.application.objects.Cluster;

import java.util.Timer;
import java.util.TimerTask;

/**
 * TimeService is the global system timer There is only one instance of this micro-service.
 * It keeps track of the amount of ticks passed since initialization and notifies
 * all other micro-services about the current time tick using {@link TickBroadcast}.
 * This class may not hold references for objects which it is not responsible for.
 * 
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class TimeService extends MicroService{

	private int speed;
	private int duration;
	private int tick;

	public TimeService(int speed, int duration) {
		super("TickService");
		this.speed = speed;
		this.duration = duration;
		this.tick = 0;
	}

	@Override
	protected void initialize() {
		// subscribe to TerminateTickBroadcast
		subscribeBroadcast(TerminateTickBroadcast.class, terminateCall -> {
			this.terminate();
			Cluster.getInstance().addFinished();
		});

		// create a task that repeats itself every tick (speed).
		// create a broadcast every second and use sendBroadcast(broadcast)
		Timer timer = new Timer("Timer");
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				if (tick >= duration) {
					// terminate the service
					sendBroadcast(new TerminateTickBroadcast());
					// generate the output file.....

					System.out.println("Here");
					timer.cancel();
				}
				else {
					tick++;
					TickBroadcast tickBroadcast = new TickBroadcast(tick);
					sendBroadcast(tickBroadcast);
				}
			}
		};
		timer.scheduleAtFixedRate(task, 200, speed);
	}

}
