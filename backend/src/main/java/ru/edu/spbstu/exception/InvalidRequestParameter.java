package ru.edu.spbstu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class InvalidRequestParameter extends RuntimeException {
    public InvalidRequestParameter(String message) {
        super(message);
    }
}
