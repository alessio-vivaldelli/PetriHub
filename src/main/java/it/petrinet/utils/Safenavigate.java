package it.petrinet.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Safenavigate {

    private static final Logger LOGGER = Logger.getLogger(Safenavigate.class.getName());

    public static void safeNavigate(Runnable navigationAction) {
        try {
            navigationAction.run();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Navigation failed", e);
        }
    }
}
