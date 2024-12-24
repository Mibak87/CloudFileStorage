package main.cloudfilestorage.exception;

public class NonUniqueFileNameException extends RuntimeException {
    public NonUniqueFileNameException(String message) {
        super(message);
    }
}
