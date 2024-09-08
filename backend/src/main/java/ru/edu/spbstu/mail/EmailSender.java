package ru.edu.spbstu.mail;

import ru.edu.spbstu.model.Language;

public interface EmailSender {
    void send(String to, String email, Language language);
}
