package it.petrinet.utils;

import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.model.TableRow.Status;
import it.petrinet.model.database.ComputationStepDAO;
import it.petrinet.view.ViewNavigator;

import java.time.LocalDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

public class NetStatusGetter {

    private final static long GTM = 2; // Offset for Europe/Rome timezone in hours
    private final static long UTC = 2;

    private static long offsetTime = GTM;

    /**
     * Determines the status of a net for current user
     */
    public static Status getStatusByComputation(Computation computation) {
        String currentUsername = ViewNavigator.getAuthenticatedUser().getUsername();


        if (computation == null) return Status.NOT_STARTED;

        if(!computation.isStarted()) return Status.NOT_STARTED;
        if(computation.isFinished()) return Status.COMPLETED;

        Computation.NEXT_STEP_TYPE nextStepType = computation.getNextStepType();
        boolean isOwner = currentUsername != null && currentUsername.equals(computation.getCreatorId());

        return switch (nextStepType) {
            case Computation.NEXT_STEP_TYPE.NONE -> Status.IN_PROGRESS;
            case Computation.NEXT_STEP_TYPE.USER -> !isOwner ? Status.WAITING : Status.IN_PROGRESS;
            case Computation.NEXT_STEP_TYPE.ADMIN -> isOwner ? Status.WAITING : Status.IN_PROGRESS;
            case Computation.NEXT_STEP_TYPE.BOTH -> Status.WAITING;
        };
    }

    /**
     * Determines the appropriate date for a net (timestamp or net date)
     */
    public static LocalDateTime determineNetDate(Computation computation, long netDate) {
        LocalDateTime creationDate = LocalDateTime.ofEpochSecond(netDate, 0, ZoneOffset.UTC).plusHours(offsetTime);
        if(computation == null) return creationDate;

        ComputationStep step = ComputationStepDAO.getLastComputationStep(computation);
        if(step == null) return creationDate;

        return step.getDateTime();
    }

    /**
     * Sets the offset time for date calculations.
     * This is useful for testing purposes or when the timezone changes.
     *
     */
    public void GTM() {
        offsetTime = GTM;
    }
    public void UTC() {
        offsetTime = UTC;
    }


}
