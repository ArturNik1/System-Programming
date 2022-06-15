package bgu.spl.mics.example.messages;

import bgu.spl.mics.Event;

public class Example2Event implements Event<String> {
    private String senderName;

    public Example2Event(String senderName) {
        this.senderName = senderName;
    }

    public String getSenderName() {
        return senderName;
    }
}
