package bgu.spl.net.srv;

import bgu.spl.net.api.Connections;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl<T> implements Connections<T> {
    private ConcurrentHashMap<Integer,ConnectionHandler<T>> idToHandelr;
    private AtomicInteger idCounter;

    public ConnectionsImpl()
    {
        this.idToHandelr=new ConcurrentHashMap<>();
        idCounter=new AtomicInteger(0);
    }
    @Override
    public boolean send(int connectionId, T msg) {

        idToHandelr.get(connectionId).send(msg);

        return true;

    }

    @Override
    public void broadcast(T msg) {
        for(ConnectionHandler<T> con:idToHandelr.values())
        {
            con.send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        idToHandelr.remove(connectionId);
    }

    public Integer register(ConnectionHandler<T> connectionHandler)
    {
        Integer id=idCounter.getAndIncrement();
        idToHandelr.put(id,connectionHandler);
        return id;
    }

}
