package it.petrinet.petrinet.persistance;

import java.io.IOException;
import java.io.InputStream;

import it.petrinet.petrinet.model.PetriNetModel;

public interface NetParser {
  PetriNetModel parse(InputStream in) throws IOException;
}
