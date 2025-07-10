package it.petrinet.utils;

public class Validation {

    public static boolean isValidInput(String username, String password) {
        String safePattern = "^[a-zA-Z0-9_\\-.@]+$";
        return username != null && !username.isEmpty() && password != null && !password.isEmpty() &&
                username.matches(safePattern) && password.matches(safePattern);
    }

    public static boolean isValidFileName(String name){
        String invalidCharsRegex = ".*[\\\\/:*?\"<>|.,;!@#\\[\\]()=].*";
        return (name.matches(invalidCharsRegex));
    }

}
