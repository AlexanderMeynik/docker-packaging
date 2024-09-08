package ru.edu.spbstu.model;

import java.util.Date;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Builder(toBuilder = true)
public class Message {
    private Long id;
    private String sender_login;
    private String author_login;
    private Long chat_id;
    private Date date;
    private String content;
    private Boolean is_deleted;
    private Boolean is_edited;
    private Boolean is_forwarded;
}
