package ru.edu.spbstu.model.comparator;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import ru.edu.spbstu.dao.MessageRepository;
import ru.edu.spbstu.model.jpa.ChatJpa;
import ru.edu.spbstu.model.jpa.MessageJpa;

import java.util.Comparator;

@AllArgsConstructor
@Component
public class ChatJpaComparator implements Comparator<ChatJpa> {

    private final MessageRepository messageRepository;

    @Override
    public int compare(ChatJpa o1, ChatJpa o2) {
        MessageJpa message1 = messageRepository.getMessagesByChatId(o1.getId()).get(0);
        MessageJpa message2 = messageRepository.getMessagesByChatId(o2.getId()).get(0);
        if (message1.getDate().before(message2.getDate())) {
            return 1;
        }
        else if (message2.getDate().before(message1.getDate())) {
            return -1;
        }
        return 0;
    }
}
