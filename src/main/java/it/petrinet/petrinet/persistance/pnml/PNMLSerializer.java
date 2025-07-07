package it.petrinet.petrinet.persistance.pnml;

import it.petrinet.petrinet.model.Node;
import it.petrinet.petrinet.model.PetriNetModel;
import it.petrinet.petrinet.model.Place;
import it.petrinet.petrinet.model.Transition;
import it.petrinet.petrinet.persistance.NetSerializer;
import it.petrinet.petrinet.persistance.metadata.PNMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Serializer for PetriNetModel objects to PNML (Petri Net Markup Language)
 * files.
 * Implements the NetSerializer interface to provide serialization logic
 * for saving Petri nets in a standardized XML format.
 */
public class PNMLSerializer implements NetSerializer {

  /**
   * Serializes the given PetriNetModel to a PNML file.
   *
   * @param net the PetriNetModel to serialize
   * @throws IOException if an I/O error occurs during serialization
   */
  @Override
  public void serialize(PetriNetModel net) throws IOException {

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.newDocument();

      Element pnmlEl = doc.createElementNS(PNMLUtils.PNML_NS, "pnml");
      doc.appendChild(pnmlEl);

      Element netEl = doc.createElementNS(PNMLUtils.PNML_NS, "net");
      netEl.setAttribute("id", net.getName());
      netEl.setAttribute("type", PNMLUtils.PNML_NS);
      pnmlEl.appendChild(netEl);

      if (net.getName() != null) {
        Element nameEl = doc.createElementNS(PNMLUtils.PNML_NS, "name");
        Element textEl = doc.createElementNS(PNMLUtils.PNML_NS, "text");
        textEl.setTextContent(net.getName());
        nameEl.appendChild(textEl);
        netEl.appendChild(nameEl);
      }

      Element pageEl = doc.createElementNS(PNMLUtils.PNML_NS, "page");
      pageEl.setAttribute("id", "page1");

      for (Node n : net.getNodes()) {
        if (n instanceof Place p) {
          pageEl.appendChild(serializePlace(doc, p));
        } else if (n instanceof Transition t) {
          pageEl.appendChild(serializeTransition(doc, t));
        }
      }

      for (Map.Entry<Node, List<Node>> entry : net.getConnections().entrySet()) {
        Node fromNode = entry.getKey();
        for (Node toNode : entry.getValue()) {
          Element arcEl = doc.createElementNS(PNMLUtils.PNML_NS, "arc");
          arcEl.setAttribute("id", fromNode.getName() + "_" + toNode.getName());
          arcEl.setAttribute("source", fromNode.getName());
          arcEl.setAttribute("target", toNode.getName());
          pageEl.appendChild(arcEl);
        }
      }

      netEl.appendChild(pageEl);
      // Write to XML file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource source = new DOMSource(doc);

      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
      transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

      // Specify your local file path

      String outputPath = System.getProperty("user.dir") + "/src/main/resources/data/pnml/"
          + net.getName() + ".pnml";
      StreamResult result = new StreamResult(outputPath);

      transformer.transform(source, result);

    } catch (Exception e) {
      System.out.println(e);
    }
  }

  /**
   * Serializes a Place object to a PNML-compliant XML Element.
   *
   * @param doc the XML Document to create elements from
   * @param p   the Place to serialize
   * @return the XML Element representing the Place
   */
  private Element serializePlace(Document doc, Place p) {
    Element place = doc.createElementNS(PNMLUtils.PNML_NS, "place");
    Element placeName = doc.createElementNS(PNMLUtils.PNML_NS, "name");
    place.setAttribute("id", p.getName());
    place.setAttribute("type", p.getType().toString());
    placeName.setTextContent(p.getName());
    place.appendChild(placeName);
    if (p.getPlaceTokens() > 0) {
      Element initialMarking = doc.createElementNS(PNMLUtils.PNML_NS, "initialMarking");
      Element text = doc.createElementNS(PNMLUtils.PNML_NS, "text");
      text.setTextContent(String.valueOf(p.getPlaceTokens()));
      initialMarking.appendChild(text);
      place.appendChild(initialMarking);
    }
    if (p.getPosition() != null) {
      Element graphic = doc.createElementNS(PNMLUtils.PNML_NS, "graphics");
      Element offset = doc.createElementNS(PNMLUtils.PNML_NS, "offset");
      offset.setAttribute("x", String.valueOf(p.getPosition().getX()));
      offset.setAttribute("y", String.valueOf(p.getPosition().getY()));
      graphic.appendChild(offset);
      place.appendChild(graphic);
    }

    return place;
  }

  /**
   * Serializes a Transition object to a PNML-compliant XML Element.
   *
   * @param doc the XML Document to create elements from
   * @param t   the Transition to serialize
   * @return the XML Element representing the Transition
   */
  private Element serializeTransition(Document doc, Transition t) {
    Element transition = doc.createElementNS(PNMLUtils.PNML_NS, "transition");
    Element transitionName = doc.createElementNS(PNMLUtils.PNML_NS, "name");
    transition.setAttribute("id", t.getName());
    transition.setAttribute("type", t.getType().toString());
    transitionName.setTextContent(t.getName());
    transition.appendChild(transitionName);
    if (t.getPosition() != null) {
      Element graphic = doc.createElementNS(PNMLUtils.PNML_NS, "graphics");
      Element offset = doc.createElementNS(PNMLUtils.PNML_NS, "offset");
      offset.setAttribute("x", String.valueOf(t.getPosition().getX()));
      offset.setAttribute("y", String.valueOf(t.getPosition().getY()));
      graphic.appendChild(offset);
      transition.appendChild(graphic);
    }
    return transition;
  }
}
