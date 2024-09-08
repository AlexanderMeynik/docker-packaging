package ru.edu.spbstu.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class ChatUser {
    private String login;
    private Boolean is_admin;

    @Override
    public String toString() {
        return login;
    }
}
