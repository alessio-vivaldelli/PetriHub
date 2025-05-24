package it.petrinet.petrinet.model;

import java.util.*;

import it.petrinet.petrinet.IllegalConnectionException;

/**
 * Represents a Petri net model with nodes and arcs.
 * Maintains an adjacency list to represent the connections between nodes.
 */
public class PetriNetModel {
  private String name;
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
  public PetriNetModel(String name, List<Place> places, List<Transition> transition, List<Arc> arcs, Place startNode,
      Place finishNode)
      throws IllegalConnectionException {
    this();
    this.name = name;
    // generate warning if startNode or finishNode are null
    if (startNode == null || finishNode == null) {
      System.err.println("Warning: Start or Finish node is null. Please ensure both are set.");
    }

    for (Place node : places) {
      addNode(node);
    }

    for (Node node : transition) {
      addNode(node);
    }

    for (Arc arc : arcs) {
      addArc(arc.getFrom(), arc.getTo());
    }

  }

  public String getName() {
    return this.name;
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    getNodes().forEach(node -> {
      sb.append(node.toString()).append("\n");
    });
    // Print the connections
    sb.append("Connections:\n");
    for (Map.Entry<Node, List<Node>> entry : adjacencyList.entrySet()) {
      Node fromNode = entry.getKey();
      List<Node> toNodes = entry.getValue();
      sb.append(fromNode.getName()).append(" -> ");
      for (Node toNode : toNodes) {
        sb.append(toNode.getName()).append(", ");
      }
      sb.setLength(sb.length() - 2); // Remove the last comma and space
      sb.append("\n");
    }

    return sb.toString();
  }

  /**
   * Returns the adjacencyList.
   * 
   * @return adjacencyList
   */
  public Map<Node, List<Node>> getConnections() {
    return adjacencyList;
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
