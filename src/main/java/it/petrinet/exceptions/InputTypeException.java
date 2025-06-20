package it.petrinet.exceptions;

public class InputTypeException extends CustomException{
    private final ExceptionType type;

    public InputTypeException() {
        super();
        this.type = ExceptionType.PARAM;
    }

    public InputTypeException(String message, ExceptionType type) {
        super(message);
        this.type = type;
    }

    public InputTypeException(String message, Throwable cause, ExceptionType type) {
        super(message, cause);
        this.type = type;
    }

    public InputTypeException(Throwable cause, ExceptionType type) {
        super(cause);
        this.type = type;
    }

    public void ErrorPrinter(){
        System.err.println("Input Error: " + getType());
        if (getMessage() != null) {
            System.err.println(getMessage());
        }
        if (getCause() != null) {
            System.err.println("Cause: " + getCause());
            getCause().printStackTrace();
        }
    }

    public ExceptionType getType() {
        return type;
    }
}
