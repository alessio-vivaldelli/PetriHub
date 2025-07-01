package it.petrinet.model.database;

import it.petrinet.model.PetriNet;


public class RecentNet{
    private PetriNet net;
    private long timestamp;

    public RecentNet(PetriNet net, long timestamp) {
        this.net = net;
        this.timestamp = timestamp;
    }

    public PetriNet getNet() {
        return net;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

