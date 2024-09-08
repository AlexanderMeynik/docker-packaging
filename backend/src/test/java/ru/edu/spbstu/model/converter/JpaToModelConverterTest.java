package ru.edu.spbstu.model.converter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.edu.spbstu.model.*;
import ru.edu.spbstu.model.jpa.ChatJpa;
import ru.edu.spbstu.model.jpa.MessageJpa;
import ru.edu.spbstu.model.jpa.UserChatDetailsJpa;
import ru.edu.spbstu.model.jpa.UserJpa;

import java.util.Date;

class JpaToModelConverterTest {

    private final JpaToModelConverter jpaToModelConverter = new JpaToModelConverter();

    @Test
    void convertUserJpaToUser() {
        UserJpa userJpa = new UserJpa(
                1L,
                "login",
                "password",
                "email@mail.org",
                "image"
        );

        User expectedUser = new User(
                "login",
                "password",
                "email@mail.org"
        );

        User actualUser = jpaToModelConverter.convertUserJpaToUser(userJpa);
        Assertions.assertEquals(expectedUser, actualUser);
    }

    @Test
    void convertChatJpaToChat() {
        ChatJpa chatJpa = new ChatJpa(
                1L,
               "chatName"
        );

        Chat expectedChat = new Chat(
                1L,
                "chatName"
        );
        Chat actualChat = jpaToModelConverter.convertChatJpaToChat(chatJpa);
        Assertions.assertEquals(expectedChat, actualChat);
    }

    @Test
    void convertMessageJpaToMessage() {
        UserJpa userJpa = new UserJpa(
                1L,
                "login",
                "password",
                "email@mail.org",
                "image"
        );

        ChatJpa chatJpa = new ChatJpa(
                1L,
                "chatName"
        );

        Date date = new Date();

        MessageJpa messageJpa = new MessageJpa(
                1L,
                date,
                "message",
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.TRUE,
                userJpa,
                userJpa,
                chatJpa
        );

        Message expectedMessage = new Message(
                1L,
                "login",
                "login",
                1L,
                date,
                "message",
                Boolean.TRUE,
                Boolean.TRUE,
                Boolean.TRUE
        );
        Message actualMessage = jpaToModelConverter.convertMessageJpaToMessage(messageJpa);
        Assertions.assertEquals(expectedMessage, actualMessage);
    }

    @Test
    void convertUserChatDetailsJpaToChatUser() {
        UserJpa userJpa1 = new UserJpa(
                1L,
                "login1",
                "password",
                "email@mail.org",
                "image"
        );
        UserJpa userJpa2 = new UserJpa(
                2L,
                "login2",
                "password",
                "email@mail.org",
                "image"
        );

        ChatJpa chatJpa = new ChatJpa(
                1L,
                "chatName"
        );

        UserChatDetailsJpa userChatDetailsJpa1 = new UserChatDetailsJpa(
                1L,
                userJpa1,
                chatJpa,
                ChatRole.USER
        );
        UserChatDetailsJpa userChatDetailsJpa2 = new UserChatDetailsJpa(
                2L,
                userJpa2,
                chatJpa,
                ChatRole.ADMIN
        );

        ChatUser expectedChatUser1 = new ChatUser(
                "login1",
                Boolean.FALSE
        );
        ChatUser expectedChatUser2 = new ChatUser(
                "login2",
                Boolean.TRUE
        );

        ChatUser actualChatUser1 = jpaToModelConverter.convertUserChatDetailsJpaToChatUser(userChatDetailsJpa1);
        ChatUser actualChatUser2 = jpaToModelConverter.convertUserChatDetailsJpaToChatUser(userChatDetailsJpa2);
        Assertions.assertEquals(expectedChatUser1, actualChatUser1);
        Assertions.assertEquals(expectedChatUser2, actualChatUser2);
    }
}