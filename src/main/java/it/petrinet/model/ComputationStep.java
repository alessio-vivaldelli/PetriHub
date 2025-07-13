package it.petrinet.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single step in a Petri net computation.
 * Each step records the execution of a transition and the resulting marking
 * state.
 * Steps are naturally ordered by their timestamp.
 */
public class ComputationStep implements Comparable<ComputationStep> {
  // TODO: remove this field, it is not used
  private String TEMP_MARKING_STATE;

  private final long id;
  private final int computationId;
  private final String netId;
  private final String transitionName;
  private Map<String, Integer> markingState = new HashMap<>();
  private final long timestamp;

  /**
   * Creates a new computation step.
   *
   * @param id             unique identifier for this step
   * @param computationId  identifier of the parent computation
   * @param netId          identifier of the associated Petri net
   * @param transitionName name of the transition that was executed
   * @param markingState   serialized representation of the marking after
   *                       transition execution
   * @param timestamp      when this step occurred (Unix timestamp in seconds)
   * @throws IllegalArgumentException if any required parameter is null or empty
   */
  public ComputationStep(long id, int computationId, String netId, String transitionName, String markingState,
      long timestamp) {
//    validatePositiveId(id, "id");
//    validatePositiveId(computationId, "computationId");
    // validateRequiredString(netId, "netId");
    // validateRequiredString(transitionName, "transitionName");
    //validateRequiredString(markingState, "markingState");

    this.id = id;
    this.computationId = computationId;
    this.netId = netId;
    this.transitionName = transitionName;
    this.TEMP_MARKING_STATE = markingState;
    parseMarkingState(markingState);
    this.timestamp = timestamp;
  }

  /**
   * Creates a new computation step.
   *
   * @param computationId  identifier of the parent computation
   * @param netId          identifier of the associated Petri net
   * @param transitionName name of the transition that was executed
   * @param markingState   serialized representation of the marking after
   *                       transition execution
   * @param timestamp      when this step occurred (Unix timestamp in seconds)
   * @throws IllegalArgumentException if any required parameter is null or empty
   */
  public ComputationStep(int computationId, String netId, String transitionName, String markingState,
      long timestamp) {
    this(-1, computationId, netId, transitionName, markingState, timestamp);
  }

  public ComputationStep(int computationId, String netId, String transitionName, Map<String, Integer> markingState,
      long timestamp) {
    this(-1, computationId, netId, transitionName, "", timestamp);

    this.markingState = markingState != null ? new HashMap<>(markingState) : new HashMap<>();
    serializeMarkingState(markingState);
  }

  private void serializeMarkingState(Map<String, Integer> markingState) {
    if (markingState == null || markingState.isEmpty()) {
      return;
    }

    StringBuilder sb = new StringBuilder();
    for (Map.Entry<String, Integer> entry : markingState.entrySet()) {
      if (!sb.isEmpty()) {
        sb.append(",");
      }
      sb.append(entry.getKey()).append(":").append(entry.getValue());
    }
    this.TEMP_MARKING_STATE = sb.toString();
  }

  private void parseMarkingState(String markingState) {
    // marking state are formatted as "place1:count1,place2:count2,..."
    if (markingState == null || markingState.trim().isEmpty()) {
      return;
    }

    String[] pairs = markingState.split(",");
    for (String pair : pairs) {
      String[] parts = pair.split(":");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Invalid marking state format: " + markingState);
      }
      String place = parts[0].trim();
      int count;
      try {
        count = Integer.parseInt(parts[1].trim());
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Invalid count in marking state: " + parts[1]);
      }
      this.markingState.put(place, count);
    }

  }

  public static ComputationStep createEmptyStep(int computationId, String netId) {
    return new ComputationStep(computationId, netId, "", "", System.currentTimeMillis() / 1000);
  }

  public long getId() {
    return id;
  }

  public int getComputationId() {
    return computationId;
  }

  public String getNetId() {
    return netId;
  }

  /**
   * Returns the name of the transition that was executed in this step.
   *
   * @return transition name
   */
  public String getTransitionName() {
    return transitionName;
  }

  /**
   * Returns the serialized marking state after this step's transition execution.
   *
   * @return marking state
   */
  public Map<String, Integer> getMarkingState() {
    return markingState;
  }

  // TODO: remove this method, it is not used
  public String getTempMarkingState() {
    return TEMP_MARKING_STATE;
  }

  /**
   * Returns the timestamp when this step occurred (Unix timestamp in seconds).
   *
   * @return timestamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * Returns the step execution time as LocalDateTime in UTC.
   *
   * @return date/time of step execution
   */
  public LocalDateTime getDateTime() {
    return LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC);
  }

  /**
   * Checks if this step belongs to the specified computation.
   *
   * @param computationId the computation ID to check
   * @return true if this step belongs to the computation, false otherwise
   */
  public boolean belongsToComputation(int computationId) {
    return this.computationId == computationId;
  }

  /**
   * Checks if this step belongs to the specified Petri net.
   *
   * @param netId the net ID to check
   * @return true if this step belongs to the net, false otherwise
   */
  public boolean belongsToNet(String netId) {
    return !getNetId().equals(netId);
  }

  /**
   * Compares computation steps primarily by timestamp, with ID as tiebreaker.
   * This ensures consistent ordering even when steps have the same timestamp.
   *
   * @param other the step to compare with
   * @return negative, zero, or positive integer as this step is less than,
   *         equal to, or greater than the specified step
   */
  @Override
  public int compareTo(ComputationStep other) {
    if (other == null)
      return 1;

    // Primary comparison by timestamp
    int timestampComparison = Long.compare(this.timestamp, other.timestamp);

    if (timestampComparison != 0)
      return timestampComparison;

    // Secondary comparison by ID to ensure consistent ordering
    return Long.compare(this.id, other.id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    ComputationStep that = (ComputationStep) o;
    return id == that.id &&
        timestamp == that.timestamp &&
        Objects.equals(computationId, that.computationId) &&
        Objects.equals(netId, that.netId) &&
        Objects.equals(transitionName, that.transitionName) &&
        Objects.equals(markingState, that.markingState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, computationId, netId, transitionName, markingState, timestamp);
  }

  // for debugging and logging purposes
  @Override
  public String toString() {
    return String.format("ComputationStep{id=%d, computationId='%s', netId='%s', " +
        "transitionName='%s', timestamp=%d, dateTime=%s}",
        id, computationId, netId, transitionName, timestamp, getDateTime());
  }

  /**
   * for detailed logging and debugging.
   * 
   * @return detailed string representation
   */
  public String toDetailedString() {
    return String.format("Step[%d]: Transition '%s' executed at %s\n" +
        "  Computation: %s, Net: %s\n" +
        "  Resulting marking: %s",
        id, transitionName, getDateTime(), computationId, netId, markingState);
  }

  private void validateRequiredString(String value, String fieldName) {
    if (value == null || value.trim().isEmpty())
      throw new IllegalArgumentException(fieldName + " cannot be null or empty");
  }

  private void validatePositiveId(long value, String fieldName) {
    if (value <= 0)
      throw new IllegalArgumentException(fieldName + " must be positive");
  }
}
