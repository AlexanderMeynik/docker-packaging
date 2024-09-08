package ru.edu.spbstu.model.comparator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.edu.spbstu.dao.MessageRepository;
import ru.edu.spbstu.model.jpa.ChatJpa;
import ru.edu.spbstu.model.jpa.MessageJpa;

import java.util.Date;
import java.util.List;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ChatJpaComparatorTest {

    @InjectMocks
    private ChatJpaComparator chatComparator;

    @Mock
    private MessageRepository messageRepository;

    @Test
    public void compareTo_test() {
        ChatJpa chat1 = new ChatJpa(1L, "name1");
        ChatJpa chat2 = new ChatJpa(2L, "name2");
        ChatJpa chat3 = new ChatJpa(3L, "name3");
        Date date1 = new Date(100L);
        Date date2 = new Date(101L);
        MessageJpa messageJpa1 = new MessageJpa();
        messageJpa1.setDate(date1);
        MessageJpa messageJpa2 = new MessageJpa();
        messageJpa2.setDate(date2);

        given(messageRepository.getMessagesByChatId(chat1.getId())).willReturn(List.of(messageJpa1));
        given(messageRepository.getMessagesByChatId(chat2.getId())).willReturn(List.of(messageJpa2));
        given(messageRepository.getMessagesByChatId(chat3.getId())).willReturn(List.of(messageJpa1));

        Assertions.assertEquals(1, chatComparator.compare(chat1, chat2));
        Assertions.assertEquals(0, chatComparator.compare(chat1, chat3));
        Assertions.assertEquals(-1, chatComparator.compare(chat2, chat1));
    }
}
