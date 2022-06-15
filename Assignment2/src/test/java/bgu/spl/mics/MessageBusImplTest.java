package bgu.spl.mics;

import bgu.spl.mics.example.messages.Example2Event;
import bgu.spl.mics.example.messages.ExampleBroadcast;
import bgu.spl.mics.example.messages.ExampleEvent;
import bgu.spl.mics.example.services.ExampleBroadcastListenerService;
import bgu.spl.mics.example.services.ExampleEventHandlerService;
import bgu.spl.mics.example.services.ExampleMessageSenderService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageBusImplTest {
    private static MessageBus messageBus;
    private static ExampleBroadcastListenerService m3;
    private static ExampleEventHandlerService m1;
    private static ExampleMessageSenderService m2;

    private static ExampleBroadcast exampleBroadcast;
    private static ExampleEvent exampleEvent;
    private static ExampleEvent exampleEvent2;
    private static ExampleEvent exampleEvent3;
    private static Thread micro1;
    private static Thread micro2;
    private static boolean exep;

    @Before
    public void setUp() throws Exception {
        messageBus=MessageBusImpl.getInstance();
        exampleEvent=new ExampleEvent("Event");
        exampleEvent2=new ExampleEvent("Event2");
        exampleEvent3=new ExampleEvent("Event3");
        exampleBroadcast=new ExampleBroadcast("Broadcast");
        m1=new ExampleEventHandlerService("m1",new String[]{"5"});
        m2=new ExampleMessageSenderService("m2",new String[]{"event"});
        m3=new ExampleBroadcastListenerService("m3",new String[]{"8"});
        exep=false;
        micro1=new Thread(()->{
            boolean a=false;
            try{
                messageBus.awaitMessage(m1);
            }
            catch (Exception e){exep=true;}
        });
    }

    @After
    public void tearDown() throws Exception {
    }

//    @Test
    public void subscribeEvent() {
        messageBus.register(m1);
      assertFalse(messageBus.isSubscribeEvent(Example2Event.class,m1));
      messageBus.subscribeEvent(Example2Event.class,m1);
      assertTrue(messageBus.isSubscribeEvent(Example2Event.class,m1));
    }

//    @Test
    public void subscribeBroadcast() {
        messageBus.register(m1);
        assertFalse(messageBus.isSubscribeBroadcast(Broadcast.class,m1));
        messageBus.subscribeBroadcast(Broadcast.class,m1);
        assertTrue(messageBus.isSubscribeBroadcast(Broadcast.class,m1));
    }

    @Test
    public void complete() {
        messageBus.register(m1);
        Future<String> future= messageBus.sendEvent(exampleEvent);
//        assertFalse(future.isDone());
//        messageBus.complete(exampleEvent,"good");
//        assertTrue(future.isDone());
//        assertEquals("good",future.get());
    }

    @Test
    public void sendBroadcast() {
        messageBus.register(m1);
        messageBus.register(m3);
        messageBus.subscribeBroadcast(ExampleBroadcast.class,m1);
        messageBus.sendBroadcast(exampleBroadcast);
//        assertEquals(messageBus.top(m1),exampleBroadcast);
//        assertEquals(messageBus.top(m3),exampleBroadcast);
    }

//    @Test
    public void sendEvent() {
        messageBus.register(m1);
        messageBus.register(m3);
        messageBus.subscribeEvent(ExampleEvent.class,m3);
        messageBus.sendEvent(exampleEvent);
//        assertEquals(messageBus.top(m1),exampleEvent);
        messageBus.sendEvent(exampleEvent2);
        assertEquals(messageBus.top(m3),exampleEvent2);
        messageBus.sendEvent(exampleEvent3);
        assertTrue(messageBus.isEventBelongTo(m3,exampleEvent));
    }

    @Test
    public void register() {
        assertFalse(messageBus.isRegistered(m1));
        messageBus.register(m1);
        assertTrue(messageBus.isRegistered(m1));
    }

    @Test
    public void unregister() {
        messageBus.unregister(m1);
        assertFalse(messageBus.isRegistered(m1));
        messageBus.register(m1);
        messageBus.unregister(m1);
        assertFalse(messageBus.isRegistered(m1));
        messageBus.register(m2);
//        assertNull(m2.sendEvent(exampleEvent));
    }

//    @Test
    public void awaitMessage() {
        try {
//            assertNull(messageBus.awaitMessage(m1));
            messageBus.register(m1);
            m2.sendEvent(exampleEvent);
            int prevSize= messageBus.getSize(m1);
            assertEquals(exampleEvent,messageBus.awaitMessage(m1));
            assertNull(messageBus.top(m1));
            assertEquals(prevSize-1,messageBus.getSize(m1));
        }catch (InterruptedException e){fail();}
            messageBus.register(m1);
            micro1.start();
            try {
                Thread.sleep(100);
            }catch (Exception e){}
            assertEquals(Thread.State.WAITING,micro1.getState());
            micro1.interrupt();
            assertTrue(exep);
    }

}