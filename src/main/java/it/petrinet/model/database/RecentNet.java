package it.petrinet.model.database;

import it.petrinet.model.PetriNet;


public class RecentNet{
    private PetriNet net;
    private Long timestamp;

    public RecentNet(PetriNet net, Long timestamp) {
        this.net = net;
        this.timestamp = timestamp;
    }

    public PetriNet getNet() {
        return net;
    }

    public Long getTimestamp() {
        return timestamp;
    }
}

