package it.petrinet.model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Represents a computation execution within a Petri net system.
 *
 * <p>
 * A computation tracks the execution of a Petri net from start to finish,
 * maintaining
 * an ordered collection of computation steps that represent transitions fired
 * during execution.
 * Each computation has a clear lifecycle: Created → Started → Finished.
 *
 * <p>
 * <strong>Lifecycle States:</strong>
 * <ul>
 * <li><strong>Created:</strong> Computation object exists but execution hasn't
 * begun</li>
 * <li><strong>Started:</strong> Execution has begun, steps can be added</li>
 * <li><strong>Finished:</strong> Execution completed, no more steps can be
 * added</li>
 * </ul>
 *
 * <p>
 * <strong>Thread Safety:</strong> This class is not thread-safe. External
 * synchronization
 * is required if accessed from multiple threads.
 *
 * @author Petri Net System
 * @version 1.0
 * @since 1.0
 */
public class Computation {

  public enum NEXT_STEP_TYPE {
    NONE,
    USER,
    ADMIN,
    BOTH
  }

  private final String netId;
  private final String creatorId;
  private final String userId;
  private long startTimestamp;
  private long endTimestamp;
  private final TreeSet<ComputationStep> steps;
  private NEXT_STEP_TYPE nextStepType = NEXT_STEP_TYPE.NONE;

  /**
   * Creates a new computation in the Created state.
   *
   * <p>
   * The computation is not started and has no timestamps set. Use
   * {@link #start()}
   * or {@link #start(long)} to begin execution.
   *
   * @param netId     the unique identifier of the associated Petri net, must not
   *                  be null or empty
   * @param creatorId the unique identifier of the user who created this
   *                  computation, must not be null or empty
   * @param userId    the unique identifier of the user executing this
   *                  computation, must not be null or empty
   * @throws IllegalArgumentException if any parameter is null or empty
   * @see #start()
   * @see #start(long)
   */
  public Computation(String netId, String creatorId, String userId) {
    validateRequiredString(netId, "netId");
    validateRequiredString(creatorId, "creatorId");
    validateRequiredString(userId, "userId");

    this.netId = netId;
    this.creatorId = creatorId;
    this.userId = userId;
    this.startTimestamp = -1L;
    this.endTimestamp = -1L;
    this.steps = new TreeSet<>();
  }

  /**
   * Creates a computation with specified timestamps, typically used when loading
   * from persistence.
   *
   * @param netId          the unique identifier of the associated Petri net, must
   *                       not be null or empty
   * @param creatorId      the unique identifier of the user who created this
   *                       computation, must not be null or empty
   * @param userId         the unique identifier of the user executing this
   *                       computation, must not be null or empty
   * @param startTimestamp the start time as Unix timestamp in seconds, or -1 if
   *                       not started
   * @param endTimestamp   the end time as Unix timestamp in seconds, or -1 if not
   *                       finished
   * @throws IllegalArgumentException if any string parameter is null or empty, or
   *                                  if endTimestamp is before startTimestamp
   *                                  when both are positive
   */
  public Computation(String netId, String creatorId, String userId, long startTimestamp, long endTimestamp,
      NEXT_STEP_TYPE nextStep) {
    this(netId, creatorId, userId);

    if (startTimestamp > 0 && endTimestamp > 0 && endTimestamp < startTimestamp)
      throw new IllegalArgumentException("End timestamp cannot be before start timestamp");

    this.startTimestamp = startTimestamp;
    this.endTimestamp = endTimestamp;
    this.nextStepType = nextStep;
  }

  /**
   * Creates a computation with specified timestamps, typically used when loading
   * from persistence.
   *
   * @param netId          the unique identifier of the associated Petri net, must
   *                       not be null or empty
   * @param creatorId      the unique identifier of the user who created this
   *                       computation, must not be null or empty
   * @param userId         the unique identifier of the user executing this
   *                       computation, must not be null or empty
   * @param startTimestamp the start time as Unix timestamp in seconds, or -1 if
   *                       not started
   * @throws IllegalArgumentException if any string parameter is null or empty, or
   *                                  if endTimestamp is before startTimestamp
   *                                  when both are positive
   */
  public Computation(String netId, String creatorId, String userId, long startTimestamp, NEXT_STEP_TYPE nextStep) {
    this(netId, creatorId, userId);

    if (startTimestamp < 0)
      throw new IllegalArgumentException("Start timestamp cannot be negative");

    this.startTimestamp = startTimestamp;
    this.endTimestamp = -1L;
    this.nextStepType = nextStep;
  }

  /**
   * Starts the computation using the current system timestamp.
   *
   * <p>
   * Transitions the computation from Created to Started state. Once started,
   * computation steps can be added and the computation can eventually be
   * finished.
   *
   * @throws IllegalStateException if the computation is already started
   * @see #start(long)
   * @see #isStarted()
   */
  public void start() {
    if (isStarted())
      throw new IllegalStateException("Computation is already started");
    this.startTimestamp = Instant.now().getEpochSecond();
  }

  /**
   * Starts the computation with the specified timestamp.
   *
   * @param startTimestamp the start time as Unix timestamp in seconds
   * @throws IllegalStateException if the computation is already started
   * @see #start()
   * @see #isStarted()
   */
  public void start(long startTimestamp) {
    if (isStarted())
      throw new IllegalStateException("Computation is already started");

    this.startTimestamp = startTimestamp;
  }

  /**
   * Finishes the computation using the current system timestamp.
   *
   * @throws IllegalStateException if the computation is not started or is already
   *                               finished
   * @see #finish(long)
   * @see #isFinished()
   * @see #getDurationSeconds()
   */
  public void finish() {
    if (!isStarted())
      throw new IllegalStateException("Computation must be started before finishing");
    if (isFinished())
      throw new IllegalStateException("Computation is already finished");

    this.endTimestamp = Instant.now().getEpochSecond();
  }

  /**
   * Finishes the computation with the specified timestamp.
   *
   * @param endTimestamp the end time as Unix timestamp in seconds
   * @throws IllegalStateException    if the computation is not started or is
   *                                  already finished
   * @throws IllegalArgumentException if endTimestamp is before the start
   *                                  timestamp
   * @see #finish()
   * @see #isFinished()
   * @see #getDurationSeconds()
   */
  public void finish(long endTimestamp) {
    if (!isStarted())
      throw new IllegalStateException("Computation must be started before finishing");
    if (isFinished())
      throw new IllegalStateException("Computation is already finished");
    if (endTimestamp < startTimestamp)
      throw new IllegalArgumentException("End timestamp cannot be before start timestamp");

    this.endTimestamp = endTimestamp;
  }

  /**
   * Adds a computation step to this computation.
   *
   * @param stepsToAdd the computation step to add, must not be null and must
   *                   belong to the same net
   * @return true if the step was successfully added, false if the step was null
   *         or belongs to a different net
   * @see ComputationStep#getNetId()
   * @see #getSteps()
   */
  public boolean addSteps(ComputationStep... stepsToAdd) {
    if (stepsToAdd == null || Arrays.stream(stepsToAdd).anyMatch(Objects::isNull))
      return false;
    if (Arrays.stream(stepsToAdd).anyMatch(e -> e.belongsToNet(getNetId())))
      return false;

    boolean allAdded = true;
    for (ComputationStep s : stepsToAdd)
      allAdded &= steps.add(s);

    return allAdded;
  }

  public boolean addStep(ComputationStep step) {
    if (step == null || step.belongsToNet(getNetId()))
      return false;

    return steps.add(step);
  }

  public boolean addSteps(Collection<ComputationStep> stepsToAdd) {
    if (stepsToAdd == null || stepsToAdd.isEmpty())
      return false;
    if (stepsToAdd.stream().anyMatch(Objects::isNull))
      return false;
    if (stepsToAdd.stream().anyMatch(e -> e.belongsToNet(getNetId())))
      return false;

    boolean allAdded = true;
    for (ComputationStep s : stepsToAdd)
      allAdded &= steps.add(s);

    return allAdded;
  }

  /**
   * Removes a computation step from this computation.
   *
   * @param step the computation step to remove
   * @return true if the step was successfully removed, false if the step was null
   *         or not found
   * @see #addStep(ComputationStep)
   * @see #clearSteps()
   */
  public boolean removeStep(ComputationStep step) {
    if (step == null)
      return false;

    return steps.remove(step);
  }

  /**
   * Removes all computation steps from this computation.
   *
   * <p>
   * This operation does not affect the computation's state
   * (Created/Started/Finished),
   * only clears the step history.
   *
   * @see #removeStep(ComputationStep)
   * @see #getStepCount()
   */
  public void clearSteps() {
    steps.clear();
  }

  /**
   * Returns an unmodifiable view of the computation steps.
   *
   * <p>
   * Steps are automatically sorted by their timestamp in ascending order.
   * The returned set cannot be modified directly; use
   * {@link #addStep(ComputationStep)}
   * and {@link #removeStep(ComputationStep)} to modify the step collection.
   *
   * @return an unmodifiable sorted set of computation steps, never null
   * @see #addStep(ComputationStep)
   * @see #getStepCount()
   */
  public SortedSet<ComputationStep> getSteps() {
    return Collections.unmodifiableSortedSet(steps);
  }

  /**
   * Returns the number of computation steps in this computation.
   *
   * @return the count of steps, always non-negative
   * @see #getSteps()
   */
  public int getStepCount() {
    return steps.size();
  }

  /**
   * Returns the unique identifier of the associated Petri net.
   *
   * @return the net ID, never null or empty
   */
  public String getNetId() {
    return netId;
  }

  /**
   * Returns the unique identifier of the user who created this computation.
   *
   * @return the creator ID, never null or empty
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * Returns the unique identifier of the user executing this computation.
   *
   * @return the user ID, never null or empty
   */
  public String getUserId() {
    return userId;
  }

  /**
   * Returns the start timestamp of this computation.
   *
   * @return the start time as Unix timestamp in seconds, or -1 if not started
   * @see #getStartDate()
   * @see #isStarted()
   */
  public long getStartTimestamp() {
    return startTimestamp;
  }

  /**
   * Returns the end timestamp of this computation.
   *
   * @return the end time as Unix timestamp in seconds, or -1 if not finished
   * @see #getEndDate()
   * @see #isFinished()
   */
  public long getEndTimestamp() {
    return endTimestamp;
  }

  /**
   * Checks if the computation has been started.
   *
   * @return true if the computation is in Started or Finished state, false if in
   *         Created state
   * @see #start()
   * @see #isFinished()
   */
  public boolean isStarted() {
    return startTimestamp != -1L;
  }

  /**
   * Checks if the computation has finished.
   *
   * @return true if the computation is in Finished state, false otherwise
   * @see #finish()
   * @see #isStarted()
   */
  public boolean isFinished() {
    return !(endTimestamp <= 0);
  }

  public long getEnd() {return endTimestamp;}

  /**
   * Returns the start time as a LocalDateTime in UTC.
   *
   * @return the start date/time in UTC timezone
   * @see #getStartTimestamp()
   * @see #isStarted()
   */
  public LocalDateTime getStartDate() {
    if (!isStarted()) return null;
    return LocalDateTime.ofEpochSecond(startTimestamp, 0, ZoneOffset.UTC);
  }

  /**
   * Returns the end time as a LocalDateTime in UTC.
   *
   * @return the end date/time in UTC timezone
   * @see #getEndTimestamp()
   * @see #isFinished()
   */
  public LocalDateTime getEndDate() {
    if (!isFinished()) return null;

    return LocalDateTime.ofEpochSecond(endTimestamp, 0, ZoneOffset.UTC);
  }

  /**
   * Returns the computation duration in seconds.
   *
   * @return the duration between start and end timestamps in seconds
   * @throws IllegalStateException if the computation is not finished
   * @see #getStartTimestamp()
   * @see #getEndTimestamp()
   * @see #isFinished()
   */
  public long getDurationSeconds() {
    if (!isFinished())
      throw new IllegalStateException("Computation is not finished yet");

    return endTimestamp - startTimestamp;
  }

  /**
   * Returns the type of the next expected step in the computation.
   *
   * @return the NEXT_STEP_TYPE enum value, indicating who should perform the next action
   */
  public NEXT_STEP_TYPE getNextStepType() {
    return nextStepType;
  }

  public static NEXT_STEP_TYPE toNextStepType(int index){
    NEXT_STEP_TYPE[] types = NEXT_STEP_TYPE.values();
    if(index>types.length | index < 0){
      throw new IllegalArgumentException();
    }
    return types[index];
  }


  /**
   * @param o the object to compare with
   * @return true if the computations are equal, false otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Computation that = (Computation) o;
    return startTimestamp == that.startTimestamp &&
        endTimestamp == that.endTimestamp &&
        Objects.equals(netId, that.netId) &&
        Objects.equals(creatorId, that.creatorId) &&
        Objects.equals(userId, that.userId);
  }

  /**
   *
   * @return the hash code value
   */
  @Override
  public int hashCode() {
    return Objects.hash(netId, creatorId, userId, startTimestamp, endTimestamp);
  }

  // for debugging and logging purposes
  @Override
  public String toString() {
    return String.format("Computation{netId='%s', creatorId='%s', userId='%s', " +
        "started=%s, finished=%s, stepCount=%d}",
        netId, creatorId, userId, isStarted(), isFinished(), steps.size());
  }

  /**
   * Validates that a string parameter is not null or empty.
   *
   * @param value     the string value to validate
   * @param fieldName the name of the field being validated (for error messages)
   * @throws IllegalArgumentException if the value is null or empty after trimming
   */
  private void validateRequiredString(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + " cannot be null or empty");
    }
  }

}
