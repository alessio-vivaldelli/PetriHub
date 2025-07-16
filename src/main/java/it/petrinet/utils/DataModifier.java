package it.petrinet.utils;

public class DataModifier {

    public static int GTM() {
        return 2 * 3600; // GMT+2 in seconds
    }

    public static int GMT() {
        return 3600; // GMT+1 in seconds
    }

    public static int UTC() {
        return 0;
    }
}
