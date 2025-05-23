package it.petrinet.petrinet.persistance;

import java.io.IOException;
import java.io.OutputStream;

import it.petrinet.petrinet.model.PetriNetModel;

public interface NetSerializer {

  void serialize(PetriNetModel net) throws IOException;

}
