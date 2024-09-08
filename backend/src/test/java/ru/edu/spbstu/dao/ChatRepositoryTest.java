package ru.edu.spbstu.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.edu.spbstu.model.ChatRole;
import ru.edu.spbstu.model.jpa.ChatJpa;
import ru.edu.spbstu.model.jpa.UserChatDetailsJpa;
import ru.edu.spbstu.model.jpa.UserJpa;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@DataJpaTest
class ChatRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private UserChatDetailsRepository userChatDetailsRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void getById() {
        ChatJpa chatJpa1 = new ChatJpa();
        chatJpa1.setName("chat Id test");
        chatJpa1 = chatRepository.save(chatJpa1);
        Optional<ChatJpa> gotten1 = chatRepository.findByIdIs(chatJpa1.getId());
        Assertions.assertTrue(gotten1.isPresent());
        Assertions.assertEquals(chatJpa1, gotten1.get());
    }

    @Test
    void getChatsByUserId() {
        UserJpa userJpa = new UserJpa();
        userJpa.setEmail("some@mail.org");
        userJpa.setImage("someImage");
        userJpa.setPassword("somePassword");
        userJpa.setLogin("someLogin");
        userJpa = userRepository.save(userJpa);

        ChatJpa chatJpa = new ChatJpa();
        chatJpa.setName("someChat");
        chatJpa = chatRepository.save(chatJpa);

        List<ChatJpa> chatsByUserId = chatRepository.getChatsByUserId(userJpa.getId());
        Assertions.assertEquals(0, chatsByUserId.size());

        UserChatDetailsJpa userChatDetailsJpa = new UserChatDetailsJpa();
        userChatDetailsJpa.setChat(chatJpa);
        userChatDetailsJpa.setUser(userJpa);
        userChatDetailsJpa.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetailsJpa);

        chatsByUserId = chatRepository.getChatsByUserId(userJpa.getId());
        Assertions.assertEquals(1, chatsByUserId.size());
        Assertions.assertEquals(chatJpa, chatsByUserId.get(0));

        ChatJpa chatJpa2 = new ChatJpa();
        chatJpa2.setName("someChat2");
        chatJpa2 = chatRepository.save(chatJpa2);

        UserChatDetailsJpa userChatDetailsJpa2 = new UserChatDetailsJpa();
        userChatDetailsJpa2.setChat(chatJpa2);
        userChatDetailsJpa2.setUser(userJpa);
        userChatDetailsJpa2.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetailsJpa2);

        chatsByUserId = chatRepository.getChatsByUserId(userJpa.getId());
        Assertions.assertEquals(2, chatsByUserId.size());
        Assertions.assertEquals(Arrays.asList(chatJpa, chatJpa2), chatsByUserId);
    }

    @Test
    void getChatsBySearch() {
        UserJpa userJpa = new UserJpa();
        userJpa.setEmail("some@mail.org");
        userJpa.setImage("someImage");
        userJpa.setPassword("somePassword");
        userJpa.setLogin("someLogin");
        userJpa = userRepository.save(userJpa);

        ChatJpa rightChatJpa1 = new ChatJpa();
        rightChatJpa1.setName("someChat2");
        rightChatJpa1 = chatRepository.save(rightChatJpa1);

        ChatJpa rightChatJpa2 = new ChatJpa();
        rightChatJpa2.setName("SoMeChat");
        rightChatJpa2 = chatRepository.save(rightChatJpa2);

        ChatJpa wrongChatJpa1 = new ChatJpa();
        wrongChatJpa1.setName("wrongChat");
        wrongChatJpa1 = chatRepository.save(wrongChatJpa1);

        ChatJpa wrongChatJpa2 = new ChatJpa();
        wrongChatJpa2.setName("anotherWrongChat");
        wrongChatJpa2 = chatRepository.save(wrongChatJpa2);

        UserChatDetailsJpa userChatDetailsJpaWrong1 = new UserChatDetailsJpa();
        userChatDetailsJpaWrong1.setChat(wrongChatJpa1);
        userChatDetailsJpaWrong1.setUser(userJpa);
        userChatDetailsJpaWrong1.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetailsJpaWrong1);

        UserChatDetailsJpa userChatDetailsJpaWrong2 = new UserChatDetailsJpa();
        userChatDetailsJpaWrong2.setChat(wrongChatJpa2);
        userChatDetailsJpaWrong2.setUser(userJpa);
        userChatDetailsJpaWrong2.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetailsJpaWrong2);

        List<ChatJpa> chatsBySearch = chatRepository.getChatsBySearch(userJpa.getId(), "some");
        Assertions.assertEquals(0, chatsBySearch.size());

        UserChatDetailsJpa userChatDetailsJpa = new UserChatDetailsJpa();
        userChatDetailsJpa.setChat(rightChatJpa1);
        userChatDetailsJpa.setUser(userJpa);
        userChatDetailsJpa.setChatRole(ChatRole.USER);
        userChatDetailsRepository.save(userChatDetailsJpa);

        UserChatDetailsJpa userChatDetailsJpa2 = new UserChatDetailsJpa();
        userChatDetailsJpa2.setChat(rightChatJpa2);
        userChatDetailsJpa2.setUser(userJpa);
        userChatDetailsJpa2.setChatRole(ChatRole.ADMIN);
        userChatDetailsRepository.save(userChatDetailsJpa2);

        chatsBySearch = chatRepository.getChatsBySearch(userJpa.getId(), "some");
        Assertions.assertEquals(2, chatsBySearch.size());
        Assertions.assertEquals(Arrays.asList(rightChatJpa2, rightChatJpa1), chatsBySearch);
    }
}