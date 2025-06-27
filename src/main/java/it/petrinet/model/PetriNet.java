package it.petrinet.model;

public class PetriNet {
    private String netName;
    private String creatorId;
    private Long creationDate;
    private String XML_PATH;
    private String image_PATH;
    private boolean isReady;

    public PetriNet(String netName, String creatorId, Long creationDate, String XML_PATH, String imagePATH, boolean isReady){
        this.netName = netName;
        this.creatorId = creatorId;
        this.creationDate = creationDate;
        this.XML_PATH = XML_PATH;
        this.image_PATH = imagePATH;
        this.isReady = isReady;
    }


    public String getNetName() {
        return netName;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public Long getCreationDate() {
        return creationDate;
    }

    public String getXML_PATH() {
        return XML_PATH;
    }

    public String getImage_PATH() {
        return image_PATH;
    }

    public boolean isReady() {
        return isReady;
    }
}
