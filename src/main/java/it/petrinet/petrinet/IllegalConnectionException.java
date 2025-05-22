package it.petrinet.petrinet;

public class IllegalConnectionException extends Exception {
  public IllegalConnectionException() {
    super("The nodes connected are not compatible.");
  }

  public IllegalConnectionException(String message) {
    super(message);
  }

  public IllegalConnectionException(String message, Throwable cause) {
    super(message, cause);
  }
}
