package ru.edu.spbstu.client.exception;

public class InvalidHttpClientFactoryStateException extends RuntimeException {
    public InvalidHttpClientFactoryStateException(String message) {
        super(message);
    }
}
