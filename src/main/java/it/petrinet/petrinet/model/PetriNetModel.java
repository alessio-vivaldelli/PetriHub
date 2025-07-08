package it.petrinet.petrinet.model;

import it.petrinet.petrinet.IllegalConnectionException;

import java.util.*;

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
   * @param places the list of nodes
   * @param arcs   the list of arcs
   * @throws IllegalConnectionException
   */
  public PetriNetModel(String name, List<Place> places, List<Transition> transition, List<Arc> arcs, Place startNode,
      Place finishNode)
      throws IllegalConnectionException {
    this();
    this.name = name;
    // generate warning if startNode or finishNode are null
    if (startNode == null || finishNode == null) {
      throw new IllegalConnectionException(
          "Start and Finish must be specified for a Petri net");
    }

    Map<Node, Boolean> allNodes = new HashMap<>();
    for (Place node : places) {
      addNode(node);
      allNodes.put(node, false);
    }

    for (Node node : transition) {
      addNode(node);
      allNodes.put(node, false);
    }

    if (!adjacencyList.containsKey(startNode) || !adjacencyList.containsKey(finishNode)) {
      throw new IllegalConnectionException("Start node or finish node are not present in the petri net nodes");
    }

    for (Arc arc : arcs) {
      Node fromNode = getNodeByName(arc.getFrom());
      Node toNode = getNodeByName(arc.getTo());

      if (fromNode.equals(finishNode)) {
        throw new IllegalConnectionException("Arc cannot start from the finish node: %s".formatted(arc.getFrom()));
      }
      if (toNode.equals(startNode)) {
        throw new IllegalConnectionException("Arc cannot connect to the start node: %s".formatted(arc.getTo()));
      }
      if (!adjacencyList.containsKey(fromNode) || !adjacencyList.containsKey(toNode)) {
        throw new IllegalConnectionException("Arc connects nodes not present in the Petri net: %s->%s".formatted(
            arc.getFrom(), arc.getTo()));
      }

      allNodes.put(fromNode, true);
      allNodes.put(toNode, true);
      addArc(arc.getFrom(), arc.getTo());
    }
    if (!allNodes.values().stream().allMatch(t -> t)) {
      System.out.println("All nodes are connected: " + allNodes);
      throw new IllegalConnectionException("Some nodes are not connected.");
    }
  }

  public String getName() {
    return this.name;
  }

  public Node getStartNode() {
    return adjacencyList.keySet().stream().filter(n -> {
      if (n instanceof Place p) {
        return p.getType().equals(PLACE_TYPE.START);
      }
      return false;
    }).findFirst().orElse(null);
  }

  public Node getFinishNode() {
    return adjacencyList.keySet().stream().filter(n -> {
      if (n instanceof Place p) {
        return p.getType().equals(PLACE_TYPE.END);
      }
      return false;
    }).findFirst().orElse(null);
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
    if (!adjacencyList.containsKey(from) || !adjacencyList.containsKey(to)) {
      throw new IllegalConnectionException("from node or to node are not present in the net model");
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
    Node n = null;

    try {
      n = adjacencyList.keySet().stream().filter(e -> e.getName().equals(name)).findFirst().get();
    } catch (Exception e) {
      throw new IllegalArgumentException("Node with name " + name + " is not a valid Place or Transition.");
    }

    if (n == null) {
      throw new IllegalArgumentException("Node with name " + name + " is not a valid Place or Transition.");
    }

    if (n instanceof Place p) {
      return p;
    } else if (n instanceof Transition t) {
      return t;
    } else {
      throw new IllegalArgumentException("Node with name " + name + " is not a valid Place or Transition.");
    }
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

}
