package it.petrinet.petrinet.model;

import java.util.*;

import it.petrinet.petrinet.IllegalConnectionException;

/**
 * Represents a Petri net model with nodes and arcs.
 * Maintains an adjacency list to represent the connections between nodes.
 */
public class PetriNetModel {
  private Map<Node, List<Node>> adjacencyList;

  /**
   * Constructs an empty PetriNetModel.
   */
  public PetriNetModel() {
    this.adjacencyList = new HashMap<>();
  }

  /**
   * Constructs a PetriNetModel with the specified nodes and arcs.
   * 
   * @param nodes the list of nodes
   * @param arcs  the list of arcs
   * @throws IllegalConnectionException
   */
  public PetriNetModel(List<Place> places, List<Transition> transition, List<Arc> arcs)
      throws IllegalConnectionException {
    this();
    for (Node node : places) {
      addNode(node);
    }
    for (Node node : transition) {
      addNode(node);
    }

    for (Arc arc : arcs) {
      addArc(arc.getFrom(), arc.getTo());
    }
  }

  /**
   * Adds a node to the Petri net.
   * 
   * @param node the node to add
   */
  public void addNode(Node node) {
    adjacencyList.putIfAbsent(node, new ArrayList<>());
  }

  /**
   * Adds an arc from one node to another if they are compatible.
   * 
   * @param from the source node
   * @param to   the destination node
   * @throws IllegalConnectionException if the nodes are not compatible
   */
  public void addArc(Node from, Node to) throws IllegalConnectionException {
    if (!areCompatible(from, to)) {
      throw new IllegalConnectionException("Nodes %s->%s are not compatible for connection".formatted(from, to));
    }
    adjacencyList.putIfAbsent(from, new ArrayList<>());
    adjacencyList.putIfAbsent(to, new ArrayList<>());
    adjacencyList.get(from).add(to);
  }

  /**
   * Adds an arc from one node to another by their names.
   * 
   * @param from the name of the source node
   * @param to   the name of the destination node
   * @throws IllegalConnectionException if the nodes are not compatible
   */
  public void addArc(String from, String to) throws IllegalConnectionException {
    Node fromNode = getNodeByName(from);
    Node toNode = getNodeByName(to);
    addArc(fromNode, toNode);
  }

  /**
   * Returns the list of successor nodes for a given node.
   * 
   * @param node the node whose successors are to be returned
   * @return list of successor nodes
   */
  public List<Node> getSuccessors(Node node) {
    return adjacencyList.getOrDefault(node, Collections.emptyList());
  }

  /**
   * Returns the set of all nodes in the Petri net.
   * 
   * @return set of nodes
   */
  public Set<Node> getNodes() {
    return adjacencyList.keySet();
  }

  /**
   * Retrieves a node by its name.
   * 
   * @param name the name of the node
   * @return the node with the specified name
   */
  public Node getNodeByName(String name) {
    return adjacencyList.keySet().stream().filter(e -> e.getName().equals(name)).findFirst().get();
  }

  /**
   * Checks if two nodes are compatible for connection.
   * 
   * @param from the source node
   * @param to   the destination node
   * @return true if compatible, false otherwise
   */
  private boolean areCompatible(Node from, Node to) {
    return (from instanceof Place && to instanceof Transition) ||
        (from instanceof Transition && to instanceof Place);
  }

  // TODO: add setters for finish and start

}
