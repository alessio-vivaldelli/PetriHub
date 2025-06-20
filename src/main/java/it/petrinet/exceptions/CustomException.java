package it.petrinet.exceptions;

public class CustomException extends Exception {    //nel caso tutte le eccezioni dovessero avere qualcosa in comune, usiamo questa

    public CustomException() {
        super();
    }

    public CustomException(String message) {
        super(message);
    }

    // Costruttore che accetta un messaggio di errore specifico e la causa originale dell'eccezione
    public CustomException(String message, Throwable cause) {
        super(message, cause);
    }

    // Costruttore che accetta la causa originale dell'eccezione
    public CustomException(Throwable cause) {
        super(cause);
    }

    public void ErrorPrinter(){
        System.err.println("Error :");
        if (getMessage() != null) {
            System.err.println(getMessage());
        }
        if (getCause() != null) {
            System.err.println("Cause of the error" + getCause());
        }
    }
}
