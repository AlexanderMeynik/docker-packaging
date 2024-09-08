package ru.edu.spbstu.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.edu.spbstu.model.Language;

import java.util.List;

@Getter
@NoArgsConstructor
@Setter
public class CreateChatRequest {
    private String chat_name;
    private List<String> user_logins;
    private String admin_login;
    private Language language;
}
