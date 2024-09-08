package ru.edu.spbstu.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ChatUpdateRequest {
    private Long chat_id;
    private List<String> user_logins;
}
