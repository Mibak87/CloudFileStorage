package main.cloudfilestorage.exception;

public class NonUniqueUserNameException extends RuntimeException {
    public NonUniqueUserNameException(String message) {
        super(message);
    }
}
