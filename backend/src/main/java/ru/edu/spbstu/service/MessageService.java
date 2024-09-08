package ru.edu.spbstu.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.edu.spbstu.request.EditMessageRequest;
import ru.edu.spbstu.request.SendMessageRequest;
import ru.edu.spbstu.dao.ChatRepository;
import ru.edu.spbstu.dao.MessageRepository;
import ru.edu.spbstu.dao.UserRepository;
import ru.edu.spbstu.exception.InvalidRequestParameter;
import ru.edu.spbstu.exception.ResourceNotFound;
import ru.edu.spbstu.model.Message;
import ru.edu.spbstu.model.converter.JpaToModelConverter;
import ru.edu.spbstu.model.jpa.ChatJpa;
import ru.edu.spbstu.model.jpa.MessageJpa;
import ru.edu.spbstu.model.jpa.UserJpa;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MessageService {

    private final JpaToModelConverter converter;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;

    public List<Message> getMessages(Long chatId, Integer pageNumber) {
        chatRepository.findByIdIs(chatId)
                .orElseThrow(() -> new ResourceNotFound("Chat with id '" + chatId + "' was not found"));
        List<MessageJpa> messageJpaList = messageRepository.getMessagesByChatId(chatId);
        return getMessageListByPage(messageJpaList, pageNumber);
    }

    public void sendMessage(SendMessageRequest request) {
        UserJpa author = userRepository.getByLogin(request.getAuthor_login())
                .orElseThrow(() -> new ResourceNotFound("User with login '" + request.getAuthor_login() + "' was not found"));
        UserJpa sender = userRepository.getByLogin(request.getSender_login())
                .orElseThrow(() -> new ResourceNotFound("User with login '" + request.getSender_login() + "' was not found"));
        ChatJpa chat = chatRepository.findByIdIs(request.getChat_id())
                .orElseThrow(() -> new ResourceNotFound("Chat with id '" + request.getChat_id() + "' was not found"));
        MessageJpa message = new MessageJpa();
        message.setDate(new Date());
        message.setChat(chat);
        message.setAuthor(author);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setIsDeleted(false);
        message.setIsEdited(false);
        message.setIsForwarded(false);
        messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Long messageId) {
        messageRepository.findByIdIs(messageId)
                .orElseThrow(() -> new ResourceNotFound("Message with id '" + messageId + "' was not found"));
        messageRepository.deleteMessage(messageId);
    }

    public void forwardMessage(Long messageId, String senderLogin, Long chatId) {
        MessageJpa originalMessage = messageRepository.findByIdIs(messageId)
                .orElseThrow(() -> new ResourceNotFound("Message with id '" + messageId + "' was not found"));
        UserJpa sender = userRepository.getByLogin(senderLogin)
                .orElseThrow(() -> new ResourceNotFound("User with login '" + senderLogin + "' was not found"));
        ChatJpa chat = chatRepository.findByIdIs(chatId)
                .orElseThrow(() -> new ResourceNotFound("Chat with id '" + chatId + "' was not found"));
        MessageJpa newMessage = new MessageJpa();
        newMessage.setDate(new Date());
        newMessage.setChat(chat);
        newMessage.setAuthor(originalMessage.getAuthor());
        newMessage.setSender(sender);
        newMessage.setContent(originalMessage.getContent());
        newMessage.setIsDeleted(false);
        newMessage.setIsEdited(false);
        newMessage.setIsForwarded(true);
        messageRepository.save(newMessage);
    }

    @Transactional
    public void editMessage(EditMessageRequest request) {
        messageRepository.findByIdIs(request.getMessage_id())
                .orElseThrow(() -> new ResourceNotFound("Message with id '" + request.getMessage_id() + "' was not found"));
        messageRepository.editMessage(request.getMessage_id(), request.getNew_content());
    }

    private List<Message> getMessageListByPage(List<MessageJpa> messageJpaList, Integer pageNumber) {
        int pageCapacity = 50;
        if (messageJpaList.size() <= pageCapacity * (pageNumber - 1)) {
            throw new InvalidRequestParameter("There are no messages on page " + pageNumber);
        }
        else if (messageJpaList.size() < pageCapacity * pageNumber) {
            return messageJpaList.subList(pageCapacity * (pageNumber - 1), messageJpaList.size()).stream()
                    .map(converter::convertMessageJpaToMessage)
                    .collect(Collectors.toList());
        }
        return messageJpaList.subList(pageCapacity * (pageNumber - 1), pageCapacity * pageNumber).stream()
                .map(converter::convertMessageJpaToMessage)
                .collect(Collectors.toList());
    }
}
