package it.petrinet.petrinet.persistance;

import it.petrinet.petrinet.model.PetriNetModel;

import java.io.IOException;

public interface NetSerializer {

  void serialize(PetriNetModel net) throws IOException;

}
