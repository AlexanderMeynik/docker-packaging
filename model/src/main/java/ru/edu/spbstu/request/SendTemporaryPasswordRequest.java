package ru.edu.spbstu.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.edu.spbstu.model.Language;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class SendTemporaryPasswordRequest {
    private String login;
    private Language language;
}
