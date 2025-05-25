package it.petrinet.petrinet.persistance.pnml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import it.petrinet.petrinet.model.*;
import it.petrinet.petrinet.builder.*;
import it.petrinet.petrinet.persistance.NetParser;
import it.petrinet.petrinet.persistance.metadata.PNMLUtils;

public class PNMLParser implements NetParser {

  private static String PNML_NS = "http://www.pnml.org/version-2009/grammar/ptnet";

  @Override
  public PetriNetModel parse(String path) throws IOException {
    PetriNetModel netModel = new PetriNetModel();

    try {
      Document doc = PNMLUtils.addNamespaceProperly(new File(path), PNML_NS);
      // DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // factory.setNamespaceAware(true);
      // DocumentBuilder builder = factory.newDocumentBuilder();
      // Document doc = builder.parse(new File(path));
      //
      // Element netEll = (Element) doc.getElementsByTagNameNS(PNML_NS,
      // "net").item(0);

      NodeList nets = doc.getElementsByTagNameNS(PNML_NS, "net");
      if (nets.getLength() == 0)
        throw new RuntimeException("No <net> found");

      Element netEl = (Element) nets.item(0);

      Element nameEl = getChildElementNS(netEl, PNML_NS, "name");
      String netName = null;
      if (nameEl != null) {
        Element textEl = getChildElementNS(nameEl, PNML_NS, "text");
        if (textEl != null) {
          netName = textEl.getTextContent().trim();
        }
      } else {
        throw new RuntimeException("No <name> found in <net> element");
      }

      PetriNetBuilder netBuilder = new PetriNetBuilder(netName);

      Element pageEl = getChildElementNS(netEl, PNML_NS, "page");
      if (pageEl == null) {
        throw new RuntimeException("No <page> found in <net> element");
      }

      NodeList places = pageEl.getElementsByTagNameNS(PNML_NS, "place");
      for (int i = 0; i < places.getLength(); i++) {
        Element placeEl = (Element) places.item(i);
        System.out.println("ATTRIBUTE: " + placeEl.getAttribute("type"));
        PetriNetBuilder.PlaceBuilder placeBuilder = netBuilder
            .newPlace(placeEl.getAttribute("id"))
            .withType(
                (!placeEl.getAttribute("type").isEmpty())
                    ? PLACE_TYPE.valueOf(placeEl.getAttribute("type").toUpperCase())
                    : PLACE_TYPE.NORMAL);
        // initialMarking element
        Element initialMarkingEl = getChildElementNS(placeEl, PNML_NS, "initialMarking");
        if (initialMarkingEl != null) {
          Element textEl = getChildElementNS(initialMarkingEl, PNML_NS, "text");
          if (textEl != null) {
            placeBuilder.initialMarking(Integer.parseInt(textEl.getTextContent().trim()));
          }
        }
        // graphic element
        Element graphicEl = getChildElementNS(placeEl, PNML_NS, "graphics");
        if (graphicEl != null) {
          Element offsetEl = getChildElementNS(graphicEl, PNML_NS, "offset");
          if (offsetEl != null) {
            int xEl = (int) Float.parseFloat(offsetEl.getAttribute("x"));
            int yEl = (int) Float.parseFloat(offsetEl.getAttribute("y"));
            placeBuilder.withPosition(xEl, yEl);
          }
        }
        netBuilder = placeBuilder.donePlace();
      }

      NodeList transitions = pageEl.getElementsByTagNameNS(PNML_NS, "transition");
      for (int i = 0; i < transitions.getLength(); i++) {
        Element transitionEl = (Element) transitions.item(i);
        PetriNetBuilder.TransitionBuilder transitionBuilder = netBuilder
            .newTransition(transitionEl.getAttribute("id"))
            .withType((!transitionEl.getAttribute("type").isEmpty())
                ? TRANSITION_TYPE.valueOf(transitionEl.getAttribute("type").toUpperCase())
                : TRANSITION_TYPE.USER);

        // graphic element
        Element graphicEl = getChildElementNS(transitionEl, PNML_NS, "graphics");
        if (graphicEl != null) {
          Element offsetEl = getChildElementNS(graphicEl, PNML_NS, "offset");
          if (offsetEl != null) {
            int xEl = (int) Float.parseFloat(offsetEl.getAttribute("x"));
            int yEl = (int) Float.parseFloat(offsetEl.getAttribute("y"));
            transitionBuilder.withPosition(xEl, yEl);
          }
        }
        netBuilder = transitionBuilder.doneTransition();
      }

      NodeList arcs = pageEl.getElementsByTagNameNS(PNML_NS, "arc");
      for (int i = 0; i < arcs.getLength(); i++) {
        Element arcEl = (Element) arcs.item(i);
        String sourceId = arcEl.getAttribute("source");
        String targetId = arcEl.getAttribute("target");
        netBuilder.addArc(sourceId, targetId);
      }

      netModel = netBuilder.build();

    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing PNML file: " + e.getMessage(), e);
    }
    return netModel;
  }

  private static Element getChildElementNS(Element parent, String ns, String tagName) {
    NodeList children = parent.getElementsByTagNameNS(ns, tagName);
    for (int i = 0; i < children.getLength(); i++) {
      if (children.item(i).getParentNode().equals(parent)) {
        return (Element) children.item(i);
      }
    }
    return null;
  }

  private static String extractText(Element parent, String ns, String field) {
    Element outer = getChildElementNS(parent, ns, field);
    if (outer == null)
      return null;
    Element text = getChildElementNS(outer, ns, "text");
    return text != null ? text.getTextContent().trim() : null;
  }

}
