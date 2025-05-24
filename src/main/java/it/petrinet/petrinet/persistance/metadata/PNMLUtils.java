package it.petrinet.petrinet.persistance.metadata;

import java.io.File;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PNMLUtils {
  public static final String PNML_NS = "http://www.pnml.org/version-2009/grammar/ptnet";

  public static boolean hasNamespace(Document doc, String namespaceUri) {
    Element root = doc.getDocumentElement();
    String existingNs = root.getNamespaceURI();
    return namespaceUri.equals(existingNs) || namespaceUri.equals(root.getAttribute("xmlns"));
  }

  public static Document addNamespaceProperly(File inputFile, String namespaceUri) throws Exception {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document oldDoc = builder.parse(inputFile);

    Element oldRoot = oldDoc.getDocumentElement();

    if (namespaceUri.equals(oldRoot.getNamespaceURI())) {
      System.out.println("Namespace già presente. Nessuna modifica.");
      return oldDoc;
    }

    System.out.println("Cahnging namespace...");

    // Crea un nuovo documento con il namespace corretto
    // Document newDoc = builder.newDocument();
    // Element newRoot = newDoc.createElementNS(namespaceUri, "pnml");

    Document newProcessedDoc = builder.newDocument(); // Documento vuoto, namespace-aware

    Element originalDocumentElement = oldDoc.getDocumentElement();
    Element newDocumentElement;

    Element newRoot = convertAndCopy(oldRoot, newProcessedDoc, namespaceUri); // PNML_NS è il tuo namespaceUri target
    newProcessedDoc.appendChild(newRoot);

    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(new DOMSource(newProcessedDoc), new StreamResult(inputFile));

    return newProcessedDoc;

    // // Copia tutto il contenuto del vecchio root nel nuovo
    // NodeList children = oldRoot.getChildNodes();
    // for (int i = 0; i < children.getLength(); i++) {
    // Node imported = newDoc.importNode(children.item(i), true);
    // newRoot.appendChild(imported);
    // }
    //
    // newDoc.appendChild(newRoot);
    //
    // // Salva su file
    // Transformer transformer = TransformerFactory.newInstance().newTransformer();
    // transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    // transformer.transform(new DOMSource(newDoc), new StreamResult(inputFile));

    // Ritorna il documento ricaricato
    // return builder.parse(inputFile);
  }

  private static Element convertAndCopy(Element oldElem, Document newDoc, String targetNsUri) {
    Element newElem = newDoc.createElementNS(targetNsUri, oldElem.getNodeName());

    // Copia attributi (eccetto xmlns, gestito da createElementNS)
    NamedNodeMap attributes = oldElem.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      Node attr = attributes.item(i);
      if (!attr.getNodeName().startsWith("xmlns")) {
        if (attr.getNamespaceURI() != null) {
          // Attributi con namespace (es. xml:lang, xlink:href)
          newElem.setAttributeNS(attr.getNamespaceURI(), attr.getNodeName(), attr.getNodeValue());
        } else {
          newElem.setAttribute(attr.getNodeName(), attr.getNodeValue());
        }
      }
    }

    // Copia ricorsivamente i nodi figli
    NodeList children = oldElem.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      if (child.getNodeType() == Node.ELEMENT_NODE) {
        newElem.appendChild(convertAndCopy((Element) child, newDoc, targetNsUri));
      } else {
        newElem.appendChild(newDoc.importNode(child, true)); // Copia testo, commenti, ecc.
      }
    }
    return newElem;
  }

}
