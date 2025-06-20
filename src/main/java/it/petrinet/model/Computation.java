package it.petrinet.model;

public class Computation {
    private static String netId;
    private static String creatorId;
    private static String userId;
    private static int startDate;
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

    public Computation() {
        this.netId = "s";



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

    public static void setEndDate(int endDate) {
        Computation.endDate = endDate;
    }

    public static int getEndDate() {
        return endDate;
    }

    public static int getStartDate(){
        return startDate;
    }
}
