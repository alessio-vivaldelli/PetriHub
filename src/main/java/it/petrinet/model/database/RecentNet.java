package it.petrinet.model.database;

import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;


public class RecentNet{
    private PetriNet net;
    private Long timestamp;
    private Computation computation = null;


    public RecentNet(PetriNet net, Long timestamp){
        this.net = net;
        this.timestamp = timestamp;
    }
    public RecentNet(PetriNet net, Long timestamp, Computation computation) {
        this(net, timestamp);
        this.computation = computation;
    }

    public PetriNet getNet() {
        return net;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Computation getComputation(){
        return computation;
    }

    public void setComputation(Computation computation) {
        this.computation = computation;
    }
}

