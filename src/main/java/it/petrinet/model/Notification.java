package it.petrinet.model;

public class Notification {

    private String sender;
    private String recipient;
    private String netId;
    private int type;
    private String title;
    private String text;
    private int timestamp;

    public Notification() {
        this.sender= "sender";
        this.recipient = "io";
        this.netId = "d";
        this.type = 0;
        this.title = "title";
        this.text = "text";
        this.timestamp = 000002025234501;
    }

    public Notification(String sender, String recipient, String netId, int type, String title, String text, int timestamp) {
        this.sender= sender;
        this.recipient = recipient;
        this.netId = netId;
        this.type = type;
        this.title = title;
        this.text = text;
        this.timestamp = timestamp;
    }


    public String getSender() {
        return sender;
    }

    public String getRecipient(){
        return recipient;
    }

    public String getNetId() {
        return netId;
    }

    public int getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public int getTimestamp(){
        return timestamp;
    }
}
