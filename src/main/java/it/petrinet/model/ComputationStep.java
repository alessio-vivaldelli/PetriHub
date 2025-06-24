package it.petrinet.model;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Represents a single step in a Petri net computation.
 * Each step records the execution of a transition and the resulting marking state.
 * Steps are naturally ordered by their timestamp.
 */
public class ComputationStep implements Comparable<ComputationStep> {
    private final long id;
    private final int computationId;
    private final String netId;
    private final String transitionName;
    private final String markingState;
    private final long timestamp;

    /**
     * Creates a new computation step.
     *
     * @param id unique identifier for this step
     * @param computationId identifier of the parent computation
     * @param netId identifier of the associated Petri net
     * @param transitionName name of the transition that was executed
     * @param markingState serialized representation of the marking after transition execution
     * @param timestamp when this step occurred (Unix timestamp in seconds)
     * @throws IllegalArgumentException if any required parameter is null or empty
     */
    public ComputationStep(long id, int computationId, String netId, String transitionName, String markingState, long timestamp) {
        validatePositiveId(id, "id");
        validatePositiveId(computationId, "computationId");
        validateRequiredString(netId, "netId");
        validateRequiredString(transitionName, "transitionName");
        validateRequiredString(markingState, "markingState");

        this.id = id;
        this.computationId = computationId;
        this.netId = netId;
        this.transitionName = transitionName;
        this.markingState = markingState;
        this.timestamp = timestamp;
    }

    /**
     * Returns the unique identifier for this step.
     *
     * @return step ID
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the identifier of the parent computation.
     *
     * @return computation ID
     */
    public int getComputationId() {
        return computationId;
    }

    /**
     * Returns the identifier of the associated Petri net.
     *
     * @return net ID
     */
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
    public String getMarkingState() {
        return markingState;
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
        return this.netId.equals(netId);
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
        if (other == null) return 1;

        // Primary comparison by timestamp
        int timestampComparison = Long.compare(this.timestamp, other.timestamp);

        if (timestampComparison != 0) return timestampComparison;

        // Secondary comparison by ID to ensure consistent ordering
        return Long.compare(this.id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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

    //for debugging and logging purposes
    @Override
    public String toString() {
        return String.format("ComputationStep{id=%d, computationId='%s', netId='%s', " +
                        "transitionName='%s', timestamp=%d, dateTime=%s}",
                id, computationId, netId, transitionName, timestamp, getDateTime());
    }

    /**
     * for detailed logging and debugging.
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
        if (value <= 0) throw new IllegalArgumentException(fieldName + " must be positive");
    }
}