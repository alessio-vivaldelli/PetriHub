package it.petrinet.model;

public class PetriNet {
    private String netName;
    private String XML_PATH;
    private String creatorId;
    private int creationDate;
    private String imagePATH;
    private boolean isReady;


    public String getNetName() {
        return netName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public int getCreationDate() {
        return creationDate;
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
}
