package it.petrinet.petrinet;

import java.util.*;

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
      throw new IllegalConnectionException();
    }
    adjacencyList.putIfAbsent(from, new ArrayList<>());
    adjacencyList.putIfAbsent(to, new ArrayList<>());
    adjacencyList.get(from).add(to);
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
