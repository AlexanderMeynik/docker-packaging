package ru.edu.spbstu.model.converter;

import org.springframework.stereotype.Service;
import ru.edu.spbstu.model.*;
import ru.edu.spbstu.model.jpa.ChatJpa;
import ru.edu.spbstu.model.jpa.MessageJpa;
import ru.edu.spbstu.model.jpa.UserChatDetailsJpa;
import ru.edu.spbstu.model.jpa.UserJpa;

@Service
public class JpaToModelConverter {
    public User convertUserJpaToUser(UserJpa userJpa) {
        User user = new User();
        user.setLogin(userJpa.getLogin());
        user.setEmail(userJpa.getEmail());
        user.setPassword(userJpa.getPassword());
        return user;
    }

    public Chat convertChatJpaToChat(ChatJpa chatJpa) {
        Chat chat = new Chat();
        chat.setId(chatJpa.getId());
        chat.setName(chatJpa.getName());
        return chat;
    }

    public Message convertMessageJpaToMessage(MessageJpa messageJpa) {
        Message message = new Message();
        message.setId(messageJpa.getId());
        message.setDate(messageJpa.getDate());
        message.setContent(messageJpa.getContent());
        message.setChat_id(messageJpa.getChat().getId());
        message.setSender_login(messageJpa.getSender().getLogin());
        message.setAuthor_login(messageJpa.getAuthor().getLogin());
        message.setIs_deleted(messageJpa.getIsDeleted());
        message.setIs_edited(messageJpa.getIsEdited());
        message.setIs_forwarded(messageJpa.getIsForwarded());
        return message;
    }

    public ChatUser convertUserChatDetailsJpaToChatUser(UserChatDetailsJpa userChatDetailsJpa) {
        ChatUser chatUser = new ChatUser();
        chatUser.setLogin(userChatDetailsJpa.getUser().getLogin());
        chatUser.setIs_admin(userChatDetailsJpa.getChatRole().equals(ChatRole.ADMIN));
        return chatUser;
    }
}
