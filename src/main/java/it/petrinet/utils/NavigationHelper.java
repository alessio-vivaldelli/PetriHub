package it.petrinet.utils;

import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.model.PetriNet;
import it.petrinet.model.database.ComputationStepDAO;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.view.ViewNavigator;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.petrinet.utils.ConstantPath.netDirectory;
import static it.petrinet.utils.Safenavigate.safeNavigate;

/**
 * Utility class for common navigation operations across controllers
 */
public class NavigationHelper {

    private static final Logger LOGGER = Logger.getLogger(NavigationHelper.class.getName());

    /**
     * Sets up navigation to net visual view for current user
     */
    public static void setupNavigationToNetVisualForCurrentUser(PetriNet net)  {
        String path = netDirectory + net.getXML_PATH();
        Computation data = findUserComputation(net, ViewNavigator.getAuthenticatedUser().getUsername());
        safeNavigate(() -> ViewNavigator.navigateToNetVisual(path, data));
    }

    /**
     * Sets up navigation to net visual view for specific user
     */
    public static void setupNavigationToNetVisualForUser(PetriNet net, String userId)  {
        String path = netDirectory + net.getXML_PATH();
        Computation data = makeComputation(net, userId);
        safeNavigate(() -> ViewNavigator.navigateToNetVisual(path, data));
    }

    private static Computation makeComputation(PetriNet net, String userId)  {
        Computation data = findUserComputation(net, userId);
        List<ComputationStep> steps = ComputationStepDAO.getStepsByComputation(data);
        data.addSteps(steps);
        return data;
    }

    /**
     * Finds computation data for specific user and net
     */
    public static Computation findUserComputation(PetriNet net, String userId)  {
        return ComputationsDAO.getComputationsByNet(net)
                .stream()
                .filter(computation -> computation.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Safely executes navigation with error handling
     */
    public static void safeNavigateWithLogging(Runnable navigationAction, String actionDescription) {
        try {
            safeNavigate(navigationAction);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Navigation failed for: " + actionDescription, e);
        }
    }
}