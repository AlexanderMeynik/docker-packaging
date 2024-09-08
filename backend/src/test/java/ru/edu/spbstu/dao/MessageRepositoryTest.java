package ru.edu.spbstu.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.edu.spbstu.model.jpa.ChatJpa;
import ru.edu.spbstu.model.jpa.MessageJpa;
import ru.edu.spbstu.model.jpa.UserJpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@DataJpaTest
public class MessageRepositoryTest {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    void getById() {
        UserJpa userJpa = new UserJpa();
        userJpa.setLogin("login");
        userJpa.setPassword("password");
        userJpa.setEmail("e@mail.ru");
        userJpa.setImage("image");
        userJpa = userRepository.save(userJpa);

        ChatJpa chatJpa = new ChatJpa();
        chatJpa.setName("name");
        chatJpa = chatRepository.save(chatJpa);

        MessageJpa messageJpa = new MessageJpa();
        messageJpa.setDate(new Date());
        messageJpa.setAuthor(userJpa);
        messageJpa.setSender(userJpa);
        messageJpa.setContent("content");
        messageJpa.setChat(chatJpa);
        messageJpa.setIsDeleted(Boolean.FALSE);
        messageJpa.setIsEdited(Boolean.FALSE);
        messageJpa.setIsForwarded(Boolean.FALSE);
        messageJpa = messageRepository.save(messageJpa);

        Optional<MessageJpa> byId = messageRepository.findByIdIs(messageJpa.getId());
        Assertions.assertTrue(byId.isPresent());
        Assertions.assertEquals(messageJpa, byId.get());
    }

    @Test
    void getMessagesByChatId() {
        UserJpa userJpa = new UserJpa();
        userJpa.setLogin("login");
        userJpa.setPassword("password");
        userJpa.setEmail("e@mail.ru");
        userJpa.setImage("image");
        userJpa = userRepository.save(userJpa);

        ChatJpa chatJpa1 = new ChatJpa();
        chatJpa1.setName("name");
        chatJpa1 = chatRepository.save(chatJpa1);
        ChatJpa chatJpa2 = new ChatJpa();
        chatJpa2.setName("name2");
        chatJpa2 = chatRepository.save(chatJpa2);

        MessageJpa messageJpa1 = new MessageJpa();
        messageJpa1.setDate(new Date());
        messageJpa1.setAuthor(userJpa);
        messageJpa1.setSender(userJpa);
        messageJpa1.setContent("content");
        messageJpa1.setChat(chatJpa1);
        messageJpa1.setIsDeleted(Boolean.FALSE);
        messageJpa1.setIsEdited(Boolean.FALSE);
        messageJpa1.setIsForwarded(Boolean.FALSE);
        messageJpa1 = messageRepository.save(messageJpa1);
        MessageJpa messageJpa2 = new MessageJpa();
        messageJpa2.setDate(new Date());
        messageJpa2.setAuthor(userJpa);
        messageJpa2.setSender(userJpa);
        messageJpa2.setContent("content2");
        messageJpa2.setChat(chatJpa2);
        messageJpa2.setIsDeleted(Boolean.FALSE);
        messageJpa2.setIsEdited(Boolean.FALSE);
        messageJpa2.setIsForwarded(Boolean.FALSE);
        messageJpa2 = messageRepository.save(messageJpa2);

        List<MessageJpa> byChatId = messageRepository.getMessagesByChatId(chatJpa1.getId());
        Assertions.assertEquals(1, byChatId.size());
        Assertions.assertEquals(List.of(messageJpa1), byChatId);
        byChatId = messageRepository.getMessagesByChatId(chatJpa2.getId());
        Assertions.assertEquals(1, byChatId.size());
        Assertions.assertEquals(List.of(messageJpa2), byChatId);
    }

    @Test
    void deleteMessage() {
        UserJpa userJpa = new UserJpa();
        userJpa.setLogin("login");
        userJpa.setPassword("password");
        userJpa.setEmail("e@mail.ru");
        userJpa.setImage("image");
        userJpa = userRepository.save(userJpa);

        ChatJpa chatJpa = new ChatJpa();
        chatJpa.setName("name");
        chatJpa = chatRepository.save(chatJpa);

        MessageJpa messageJpa = new MessageJpa();
        messageJpa.setDate(new Date());
        messageJpa.setAuthor(userJpa);
        messageJpa.setSender(userJpa);
        messageJpa.setContent("content");
        messageJpa.setChat(chatJpa);
        messageJpa.setIsDeleted(Boolean.FALSE);
        messageJpa.setIsEdited(Boolean.FALSE);
        messageJpa.setIsForwarded(Boolean.FALSE);
        messageJpa = messageRepository.save(messageJpa);

        messageRepository.deleteMessage(messageJpa.getId());
        entityManager.clear();
        Optional<MessageJpa> byId = messageRepository.findByIdIs(messageJpa.getId());
        Assertions.assertTrue(byId.isPresent());
        Assertions.assertTrue(byId.get().getIsDeleted());
    }

    @Test
    void deleteAllByChat_Id() {
        UserJpa userJpa = new UserJpa();
        userJpa.setLogin("login");
        userJpa.setPassword("password");
        userJpa.setEmail("e@mail.ru");
        userJpa.setImage("image");
        userJpa = userRepository.save(userJpa);

        ChatJpa chatJpa = new ChatJpa();
        chatJpa.setName("name");
        chatJpa = chatRepository.save(chatJpa);

        MessageJpa messageJpa = new MessageJpa();
        messageJpa.setDate(new Date());
        messageJpa.setAuthor(userJpa);
        messageJpa.setSender(userJpa);
        messageJpa.setContent("content");
        messageJpa.setChat(chatJpa);
        messageJpa.setIsDeleted(Boolean.FALSE);
        messageJpa.setIsEdited(Boolean.FALSE);
        messageJpa.setIsForwarded(Boolean.FALSE);
        messageJpa = messageRepository.save(messageJpa);

        messageRepository.deleteAllByChat_Id(chatJpa.getId());
        testEntityManager.flush();
        Assertions.assertNull(testEntityManager.find(MessageJpa.class, messageJpa.getId()));
    }

    @Test
    void editMessage() {
        UserJpa userJpa = new UserJpa();
        userJpa.setLogin("login");
        userJpa.setPassword("password");
        userJpa.setEmail("e@mail.ru");
        userJpa.setImage("image");
        userJpa = userRepository.save(userJpa);

        ChatJpa chatJpa = new ChatJpa();
        chatJpa.setName("name");
        chatJpa = chatRepository.save(chatJpa);

        MessageJpa messageJpa = new MessageJpa();
        messageJpa.setDate(new Date());
        messageJpa.setAuthor(userJpa);
        messageJpa.setSender(userJpa);
        messageJpa.setContent("content");
        messageJpa.setChat(chatJpa);
        messageJpa.setIsDeleted(Boolean.FALSE);
        messageJpa.setIsEdited(Boolean.FALSE);
        messageJpa.setIsForwarded(Boolean.FALSE);
        messageJpa = messageRepository.save(messageJpa);

        String newContent = "new content";
        messageRepository.editMessage(messageJpa.getId(), newContent);
        entityManager.clear();
        Optional<MessageJpa> byId = messageRepository.findByIdIs(messageJpa.getId());
        Assertions.assertTrue(byId.isPresent());
        Assertions.assertTrue(byId.get().getIsEdited());
        Assertions.assertEquals(newContent, byId.get().getContent());
    }
}
