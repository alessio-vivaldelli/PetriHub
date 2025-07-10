package it.petrinet.model;

public class Notification {

    private String sender;
    private String receiver;
    private String netId;
    private int type;
    private long timestamp;

    public Notification() {
        this.sender= "unknown sender";
        this.receiver = "unknown receiver";
        this.netId = "id";
        this.type = 0;
        this.timestamp = 0;
    }

    public Notification(String sender, String receiver, String netId, int type, long timestamp) {
        this.sender= sender;
        this.receiver = receiver;
        this.netId = netId;
        this.type = type;
        this.timestamp = timestamp;
    }

  public String getSender() {
    return sender;
  }

  public String getReceiver(){
        return receiver;
    }

  public String getNetId() {
    return netId;
  }

  public int getType() {
    return type;
  }

  public long getTimestamp(){
        return timestamp;
    }
}
