package it.petrinet.model;

public class PetriNet {
    private int netId;
    private String netName;
    private String XML_PATH;
    private String creatorId;
    private String imagePATH;
    private boolean isReady;


    public String getNetName() {
        return netName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getXML_PATH() {
        return XML_PATH;
    }

    public String getImagePATH() {
        return imagePATH;
    }

    public boolean isReady() {
        return isReady;
    }

    public int getNetId() {
        return netId;
    }

}
