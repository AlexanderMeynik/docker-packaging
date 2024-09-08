package ru.edu.spbstu.client;

public class NoAlertFoundException extends AssertionError {
    @Override
    public String getMessage() {
        return "No alerts found!";
    }

}
