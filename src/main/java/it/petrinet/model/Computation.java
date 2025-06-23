package it.petrinet.model;

public class Computation {
    private String netId; //Computation ID
    private String creatorId;
    private String userId;
    private int startDate;
    private static int endDate = -1;

    public Computation(String netId, String creatorId, String userId, int startDate) {
        this.netId = netId;
        this.creatorId = creatorId;
        this.userId = userId;
        this.startDate = startDate;
    }
    public Computation(String netId, String creatorId, String userId, int startDate, int endDate) {
        this(netId,creatorId,userId,startDate);
        this.endDate = endDate;
    }
    public String getNetId() {
        return netId;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setEndDate(int endDate) {
        Computation.endDate = endDate;
    }

    public int getEndDate() {
        return endDate;
    }

    public int getStartDate(){
        return startDate;
    }
}
