package bgu.spl.mics;

import bgu.spl.mics.application.messages.TickBroadcast;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {

	private ConcurrentHashMap< Event,Future> eventFutureMap; // gets the event and finds it's future.
	private ConcurrentHashMap<Class<? extends Message>, Queue<MicroService>> eventMServiceMap; // gets an (event) and looks for the queue with all of the subscribed services to that event.
	private ConcurrentHashMap<Class<? extends Message>,Queue<MicroService>> broadMServiceMap; // gets an (broadcast) and looks for the queue with all of the subscribed services to that broadcast.
	private ConcurrentHashMap<MicroService,BlockingQueue<Message>> mQueues; // gets a microservice and looks for the queue that holds it's 'missions'
	private Object eventMSKey=new Object();
	private Object broadMSKey=new Object();

	private MessageBusImpl()
	{
		this.eventFutureMap=new ConcurrentHashMap<>();
		this.eventMServiceMap =new ConcurrentHashMap<>();
		this.broadMServiceMap=new ConcurrentHashMap<>();
		this.mQueues =new ConcurrentHashMap<>();
	}
	private static class MessageBusImpHolder{
		private static MessageBusImpl instance=new MessageBusImpl();
	}

	public static MessageBusImpl getInstance()
	{
		return MessageBusImpHolder.instance;
	}



	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// TODO Auto-generated method
		synchronized (eventMSKey){ //take control on eventMServiceMap
			eventMServiceMap.putIfAbsent(type,new LinkedList<MicroService>()); // check if it's the first time the (event) is being subscribed to. if it is, create the list.
			eventMServiceMap.get(type).add(m); // add the microservice to that list.
		}
	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// TODO Auto-generated method stub
		synchronized (broadMSKey){
			broadMServiceMap.putIfAbsent(type,new LinkedList<MicroService>()); // check if it's the first time the (broadcast) is being subscribed to. if it is, create the list.
			broadMServiceMap.get(type).add(m); // add the microservice to that list.
		}

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		// TODO Auto-generated method stub
		Future<T> future=eventFutureMap.get(e); // get the future from the table.
		future.resolve(result); // put the result in
		eventFutureMap.remove(e); // the future is not needed ?
	}

	@Override
	public void sendBroadcast(Broadcast b) {
		// TODO Auto-generated method stub
		if(broadMServiceMap.containsKey(b.getClass()))
		{
			Queue<MicroService> sendTo=broadMServiceMap.get(b.getClass());
				synchronized (broadMServiceMap.get(b.getClass())) { // making sure we won't change the list mid-iteration.--------maybe deadlock
					for (MicroService m : sendTo) {
						mQueues.get(m).add(b); // add to every subscribed party the broadcast.
					}
				}
		}
	}

	
	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		// TODO Auto-generated method stub
			if (eventMServiceMap.containsKey(e.getClass())) {
				Queue<MicroService> candidates = eventMServiceMap.get(e.getClass()); // get the queue
				synchronized (eventMServiceMap.get(e.getClass())) { //lock specific queue so we won't change/use it mid-iteration.
					if(candidates.isEmpty())return null;
					MicroService sendTo=candidates.poll();
					mQueues.get(sendTo).add(e); // add the event to the correct queue
					candidates.add(sendTo); // robin hood pattern
				}
				Future<T> future=new Future<>(); // create a new future to get the result for that event.
				eventFutureMap.put(e,future); // add the future to it's place
				return future;
			} else return null;
	}

	@Override
	public void register(MicroService m) { // creates a list of missions for the microservice.
		// TODO Auto-generated method stub
		BlockingQueue<Message> mQueue=new LinkedBlockingQueue<>();
		mQueues.put(m,mQueue);

	}

	@Override
	public void unregister(MicroService m) {
		// TODO Auto-generated method stub
		if(!isRegistered(m))return;
		synchronized (eventMSKey) // lock the event to service map
		{
			for(Class<? extends Message> i:eventMServiceMap.keySet())
			{
					synchronized (eventMServiceMap.get(i)) // should not cause a deadlock
					{
						eventMServiceMap.get(i).remove(m);
					}
			}
		}
		synchronized (broadMSKey)
		{
			for(Class<? extends Message> i:broadMServiceMap.keySet())
			{
			//	if(i.getClass()==TickBroadCast)
				synchronized (broadMServiceMap.get(i))
				{
					broadMServiceMap.get(i).remove(m);
				}
			}
		}
		mQueues.remove(m);


	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		// TODO Auto-generated method stub
		if(!isRegistered(m))
			throw new IllegalStateException();
		Message message=mQueues.get(m).take(); // takes the head of the queue, waits if there are no elements in it.
		return message;
	}

	@Override
	public boolean isSubscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		return false;
	}

	@Override
	public <T> boolean isSubscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		return false;
	}

	@Override
	public boolean isRegistered(MicroService microService) {
		return mQueues.containsKey(microService);
	}

	@Override
	public Message top(MicroService m) {
		return null;
	}

	@Override
	public int getSize(MicroService m) {
		return 0;
	}

	@Override
	public <T>boolean isEventBelongTo(MicroService m,Event<T> event) {
		return false;
	}


}
