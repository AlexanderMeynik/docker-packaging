package ru.edu.spbstu.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class EditMessageRequest {
    private Long message_id;
    private String new_content;
}
