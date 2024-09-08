package ru.edu.spbstu.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.edu.spbstu.model.ChatRole;
import ru.edu.spbstu.model.jpa.ChatJpa;
import ru.edu.spbstu.model.jpa.UserChatDetailsJpa;
import ru.edu.spbstu.model.jpa.UserJpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@DataJpaTest
class UserChatDetailsRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private UserChatDetailsRepository userChatDetailsRepository;
    @Autowired
    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void getChatMembers() {
        UserJpa user1 = new UserJpa();
        user1.setEmail("u1@mail.org");
        user1.setImage("image 1");
        user1.setPassword("password 1");
        user1.setLogin("login 1");
        user1 = userRepository.save(user1);

        UserJpa user2 = new UserJpa();
        user2.setEmail("u2@mail.org");
        user2.setImage("image 2");
        user2.setPassword("password 2");
        user2.setLogin("login 2");
        user2 = userRepository.save(user2);

        UserJpa user3 = new UserJpa();
        user3.setEmail("u3@mail.org");
        user3.setImage("image 3");
        user3.setPassword("password 3");
        user3.setLogin("login 3");
        userRepository.save(user3);

        ChatJpa chat = new ChatJpa();
        chat.setName("someChat");
        chat = chatRepository.save(chat);

        List<UserChatDetailsJpa> chatMembers = userChatDetailsRepository.getChatMembers(chat.getId());
        Assertions.assertEquals(0, chatMembers.size());

        UserChatDetailsJpa userChatDetails1 = new UserChatDetailsJpa();
        userChatDetails1.setChat(chat);
        userChatDetails1.setUser(user1);
        userChatDetails1.setChatRole(ChatRole.USER);
        userChatDetails1 = userChatDetailsRepository.save(userChatDetails1);

        chatMembers = userChatDetailsRepository.getChatMembers(chat.getId());
        Assertions.assertEquals(1, chatMembers.size());
        Assertions.assertEquals(Collections.singletonList(userChatDetails1), chatMembers);

        UserChatDetailsJpa userChatDetails2 = new UserChatDetailsJpa();
        userChatDetails2.setChat(chat);
        userChatDetails2.setUser(user2);
        userChatDetails2.setChatRole(ChatRole.USER);
        userChatDetails2 = userChatDetailsRepository.save(userChatDetails2);

        UserChatDetailsJpa userChatDetails3 = new UserChatDetailsJpa();
        userChatDetails3.setChat(chat);
        userChatDetails3.setUser(user1);
        userChatDetails3.setChatRole(ChatRole.USER);
        userChatDetails3 = userChatDetailsRepository.save(userChatDetails3);

        chatMembers = userChatDetailsRepository.getChatMembers(chat.getId());
        Assertions.assertEquals(3, chatMembers.size());
        Assertions.assertEquals(Arrays.asList(userChatDetails1, userChatDetails2, userChatDetails3), chatMembers);
    }

    @Test
    void deleteAllByChat_Id() {
        UserJpa user1 = new UserJpa();
        user1.setEmail("u1@mail.org");
        user1.setImage("image 1");
        user1.setPassword("password 1");
        user1.setLogin("login 1");
        user1 = userRepository.save(user1);

        ChatJpa chat = new ChatJpa();
        chat.setName("someChat");
        chat = chatRepository.save(chat);

        UserChatDetailsJpa userChatDetails1 = new UserChatDetailsJpa();
        userChatDetails1.setChat(chat);
        userChatDetails1.setUser(user1);
        userChatDetails1.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetails1);

        UserChatDetailsJpa userChatDetails2 = new UserChatDetailsJpa();
        userChatDetails2.setChat(chat);
        userChatDetails2.setUser(user1);
        userChatDetails2.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetails2);

        long all = userChatDetailsRepository.findAll().spliterator().getExactSizeIfKnown();
        userChatDetailsRepository.deleteAllByChat_Id(chat.getId());
        long count = userChatDetailsRepository.findAll().spliterator().getExactSizeIfKnown();
        Assertions.assertEquals(2, all);
        Assertions.assertEquals(0, count);
    }

    @Test
    void deleteByChatAndUser() {
        UserJpa user1 = new UserJpa();
        user1.setEmail("u1@mail.org");
        user1.setImage("image 1");
        user1.setPassword("password 1");
        user1.setLogin("login 1");
        user1 = userRepository.save(user1);

        ChatJpa chat = new ChatJpa();
        chat.setName("someChat");
        chat = chatRepository.save(chat);

        UserChatDetailsJpa userChatDetails1 = new UserChatDetailsJpa();
        userChatDetails1.setChat(chat);
        userChatDetails1.setUser(user1);
        userChatDetails1.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetails1);

        long all = userChatDetailsRepository.findAll().spliterator().getExactSizeIfKnown();
        userChatDetailsRepository.deleteByChatAndUser(chat, user1);
        long count = userChatDetailsRepository.findAll().spliterator().getExactSizeIfKnown();
        Assertions.assertEquals(count, all - 1);
    }

    @Test
    void makeUsersAdmins() {
        UserJpa user1 = new UserJpa();
        user1.setEmail("u1@mail.org");
        user1.setImage("image 1");
        user1.setPassword("password 1");
        user1.setLogin("login 1");
        user1 = userRepository.save(user1);

        UserJpa user2 = new UserJpa();
        user2.setEmail("u2@mail.org");
        user2.setImage("image 2");
        user2.setPassword("password 2");
        user2.setLogin("login 2");
        user2 = userRepository.save(user2);

        UserJpa user3 = new UserJpa();
        user3.setEmail("u3@mail.org");
        user3.setImage("image 3");
        user3.setPassword("password 3");
        user3.setLogin("login 3");
        user3 = userRepository.save(user3);

        ChatJpa chat = new ChatJpa();
        chat.setName("someChat");
        chat = chatRepository.save(chat);

        UserChatDetailsJpa userChatDetails1 = new UserChatDetailsJpa();
        userChatDetails1.setChat(chat);
        userChatDetails1.setUser(user1);
        userChatDetails1.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetails1);

        UserChatDetailsJpa userChatDetails2 = new UserChatDetailsJpa();
        userChatDetails2.setChat(chat);
        userChatDetails2.setUser(user2);
        userChatDetails2.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetails2);

        UserChatDetailsJpa userChatDetails3 = new UserChatDetailsJpa();
        userChatDetails3.setChat(chat);
        userChatDetails3.setUser(user3);
        userChatDetails3.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetails3);
        entityManager.clear();

        List<UserChatDetailsJpa> chatMembers = userChatDetailsRepository.getChatMembers(chat.getId());
        long adminCounter = chatMembers.stream().filter(el -> el.getChatRole().equals(ChatRole.ADMIN)).count();
        Assertions.assertEquals(0, adminCounter);

        userChatDetailsRepository.makeUsersAdmins(chat.getId(), user1.getId());
        entityManager.clear();

        chatMembers = userChatDetailsRepository.getChatMembers(chat.getId());
        adminCounter = chatMembers.stream().filter(el -> el.getChatRole().equals(ChatRole.ADMIN)).count();
        Assertions.assertEquals(1, adminCounter);

        userChatDetailsRepository.makeUsersAdmins(chat.getId(), user2.getId());
        userChatDetailsRepository.makeUsersAdmins(chat.getId(), user3.getId());
        entityManager.clear();

        chatMembers = userChatDetailsRepository.getChatMembers(chat.getId());
        adminCounter = chatMembers.stream().filter(el -> el.getChatRole().equals(ChatRole.ADMIN)).count();
        Assertions.assertEquals(3, adminCounter);
    }
}