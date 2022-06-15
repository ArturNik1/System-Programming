package bgu.spl.mics.application.messages;

import bgu.spl.mics.Broadcast;

import java.util.HashSet;

public class PublishConfrenceBroadcast implements Broadcast {

    private HashSet<String> names;

    public PublishConfrenceBroadcast(HashSet<String> names) {
        this.names = names;
    }

    public HashSet<String> getNames() {
        return this.names;
    }

}
