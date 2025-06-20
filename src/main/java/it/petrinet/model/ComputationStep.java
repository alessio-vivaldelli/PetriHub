package it.petrinet.model;

public class ComputationStep {
    private int id;
    private int computationId;
    private String netId;
    private String transition;
    private String markingLocation;
    private int timestamp;

    public ComputationStep(int id, int computationId, String netId, String transition, String markingLocation, int timestamp) {
        this.id = id;
        this.computationId = computationId;
        this.netId = netId;
        this.transition = transition;
        this.markingLocation = markingLocation;
        this.timestamp =timestamp;
    }

    public int getId() {
        return id;
    }

    public int getComputationId() {
        return computationId;
    }

    public String getNetId() {
        return netId;
    }

    public String getTransition() {
        return transition;
    }

    public String getMarkingLocation() {
        return markingLocation;
    }

    public int getTimestamp() {
        return timestamp;
    }


}
