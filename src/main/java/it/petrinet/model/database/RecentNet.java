package it.petrinet.model.database;

import it.petrinet.model.PetriNet;


public class RecentNet{
    private PetriNet net;
    private long modificationTimestamp;

    public RecentNet(PetriNet net, long modificationTimestamp) {
        this.net = net;
        this.modificationTimestamp = modificationTimestamp;
    }

    public PetriNet getNet() {
        return net;
    }

    public long getModificationTimestamp() {
        return modificationTimestamp;
    }
}

