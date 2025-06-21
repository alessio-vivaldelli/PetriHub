package it.petrinet.petrinet.builder;

import java.io.File;
import java.io.IOException;

import it.petrinet.petrinet.model.*;
import it.petrinet.petrinet.persistance.pnml.*;

public class BuilderTest {
  public static void main(String[] args) {

    PetriNetModel petriNetModel = null;
    try {
      PetriNetBuilder builder = new PetriNetBuilder("TestNet");
      petriNetModel = builder.newPlace("p1")
          .initialMarking(10)
          .donePlace()
          .newTransition("t1")
          .withType(TRANSITION_TYPE.ADMIN)
          .doneTransition()
          .addArc("p1", "t1")
          .setFinishNode("p1")
          .newPlace("p2")
          .withType(PLACE_TYPE.START)
          .donePlace()
          .build();
      System.out.println("PETRI NET CONSTRUCTED");

    } catch (Exception e) {
      System.out.println(e);
    }

    // PNMLSerializer serializer = new PNMLSerializer();
    // try {
    // serializer.serialize(petriNetModel);
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    String path = System.getProperty("user.dir") +
        "/src/main/resources/data/pnml/TEST.pnml";
    if (!(new File(path)).exists()) {
      System.out.println("FILE NOT EXIST");
    }

    System.out.println("FILE EXISTS: " + path);

    PNMLParser parser = new PNMLParser();
    try {
      PetriNetModel parsedModel = parser.parse(path);
      System.out.println("PETRI NET PARSED: " + parsedModel);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}
