package ru.edu.spbstu.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.edu.spbstu.dao.MessageRepository;
import ru.edu.spbstu.model.Language;
import ru.edu.spbstu.request.ChatUpdateRequest;
import ru.edu.spbstu.request.CreateChatRequest;
import ru.edu.spbstu.request.SendMessageRequest;
import ru.edu.spbstu.dao.ChatRepository;
import ru.edu.spbstu.dao.UserChatDetailsRepository;
import ru.edu.spbstu.dao.UserRepository;
import ru.edu.spbstu.exception.InvalidRequestParameter;
import ru.edu.spbstu.exception.ResourceNotFound;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.ChatRole;
import ru.edu.spbstu.model.ChatUser;
import ru.edu.spbstu.model.comparator.ChatJpaComparator;
import ru.edu.spbstu.model.converter.JpaToModelConverter;
import ru.edu.spbstu.model.jpa.ChatJpa;
import ru.edu.spbstu.model.jpa.UserChatDetailsJpa;
import ru.edu.spbstu.model.jpa.UserJpa;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatService {

    private final JpaToModelConverter converter;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserChatDetailsRepository userChatDetailsRepository;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final ChatJpaComparator chatComparator;

    public List<Chat> getChats(String login, Integer pageNumber) {
        UserJpa userJpa = userRepository.getByLogin(login)
                .orElseThrow(() -> new ResourceNotFound("User with login '" + login + "' was not found"));
        List<ChatJpa> userChats = chatRepository.getChatsByUserId(userJpa.getId());
        return getChatListByPage(userChats, pageNumber);
    }

    public List<Chat> getChatsBySearch(String login, String begin, Integer pageNumber) {
        UserJpa userJpa = userRepository.getByLogin(login)
                .orElseThrow(() -> new ResourceNotFound("User with login '" + login + "' was not found"));
        List<ChatJpa> userChats = chatRepository.getChatsBySearch(userJpa.getId(), begin);
        return getChatListByPage(userChats, pageNumber);
    }

    public List<ChatUser> getChatMembers(Long chatId) {
        chatRepository.findByIdIs(chatId)
                .orElseThrow(() -> new ResourceNotFound("Chat with id '" + chatId + "' was not found"));
        List<UserChatDetailsJpa> userChatDetailsJpaList = userChatDetailsRepository.getChatMembers(chatId);
        return userChatDetailsJpaList.stream().map(converter::convertUserChatDetailsJpaToChatUser)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteChat(Long chatId) {
        chatRepository.findByIdIs(chatId)
                .orElseThrow(() -> new ResourceNotFound("Chat with id '" + chatId + "' was not found"));
        userChatDetailsRepository.deleteAllByChat_Id(chatId);
        messageRepository.deleteAllByChat_Id(chatId);
        chatRepository.deleteById(chatId);
    }

    @Transactional
    public void createChat(CreateChatRequest request){
        ChatJpa chatJpa = new ChatJpa();
        chatJpa.setName(request.getChat_name());
        ChatJpa savedChatJpa = chatRepository.save(chatJpa);
        for (String login: request.getUser_logins()) {
            UserChatDetailsJpa userChatDetailsJpa = new UserChatDetailsJpa();
            UserJpa userJpa = userRepository.getByLogin(login)
                    .orElseThrow(() -> new ResourceNotFound("User with login '" + login + "' was not found"));
            userChatDetailsJpa.setChat(savedChatJpa);
            userChatDetailsJpa.setUser(userJpa);
            userChatDetailsJpa.setChatRole(ChatRole.USER);
            userChatDetailsRepository.save(userChatDetailsJpa);
        }
        UserChatDetailsJpa userChatDetailsJpa = new UserChatDetailsJpa();
        UserJpa userJpa = userRepository.getByLogin(request.getAdmin_login())
                .orElseThrow(() -> new ResourceNotFound("User with login '" + request.getAdmin_login() + "' was not found"));
        userChatDetailsJpa.setChat(savedChatJpa);
        userChatDetailsJpa.setUser(userJpa);
        userChatDetailsJpa.setChatRole(ChatRole.ADMIN);
        userChatDetailsRepository.save(userChatDetailsJpa);
        SendMessageRequest sendMessageRequest = new SendMessageRequest();
        sendMessageRequest.setSender_login(request.getAdmin_login());
        sendMessageRequest.setAuthor_login(request.getAdmin_login());
        sendMessageRequest.setChat_id(savedChatJpa.getId());
        if (request.getLanguage().equals(Language.RUSSIAN)) {
            sendMessageRequest.setContent("Создан новый чат");
        } else {
            sendMessageRequest.setContent("New chat created");
        }
        messageService.sendMessage(sendMessageRequest);
    }

    @Transactional
    public void deleteUsersFromChat(ChatUpdateRequest request) {
        ChatJpa chatJpa = chatRepository.findByIdIs(request.getChat_id())
                .orElseThrow(() -> new ResourceNotFound("Chat with id '" + request.getChat_id() + "' was not found"));
        for (String login: request.getUser_logins()) {
            UserJpa userJpa = userRepository.getByLogin(login)
                    .orElseThrow(() -> new ResourceNotFound("User with login '" + login + "' was not found"));
            userChatDetailsRepository.deleteByChatAndUser(chatJpa, userJpa);
        }
    }

    @Transactional
    public void addUsersToChat(ChatUpdateRequest request) {
        ChatJpa chatJpa = chatRepository.findByIdIs(request.getChat_id())
                .orElseThrow(() -> new ResourceNotFound("Chat with id '" + request.getChat_id() + "' was not found"));
        for (String login: request.getUser_logins()) {
            UserChatDetailsJpa userChatDetailsJpa = new UserChatDetailsJpa();
            UserJpa userJpa = userRepository.getByLogin(login)
                    .orElseThrow(() -> new ResourceNotFound("User with login '" + login + "' was not found"));
            userChatDetailsJpa.setChat(chatJpa);
            userChatDetailsJpa.setUser(userJpa);
            userChatDetailsJpa.setChatRole(ChatRole.USER);
            userChatDetailsRepository.save(userChatDetailsJpa);
        }
    }

    @Transactional
    public void makeUsersAdmins(ChatUpdateRequest request) {
        ChatJpa chatJpa = chatRepository.findByIdIs(request.getChat_id())
                .orElseThrow(() -> new ResourceNotFound("Chat with id '" + request.getChat_id() + "' was not found"));
        for (String login: request.getUser_logins()) {
            UserJpa userJpa = userRepository.getByLogin(login)
                    .orElseThrow(() -> new ResourceNotFound("User with login '" + login + "' was not found"));
            userChatDetailsRepository.makeUsersAdmins(chatJpa.getId(), userJpa.getId());
        }
    }

    private List<Chat> getChatListByPage(List<ChatJpa> chatJpaList, Integer pageNumber) {
        int pageCapacity = 20;
        if (chatJpaList.size() <= pageCapacity * (pageNumber - 1)) {
            throw new InvalidRequestParameter("There are no chats on page " + pageNumber);
        }
        else if (chatJpaList.size() < pageCapacity * pageNumber) {
            chatJpaList.sort(chatComparator);
            return chatJpaList.subList(pageCapacity * (pageNumber - 1), chatJpaList.size()).stream()
                    .map(converter::convertChatJpaToChat)
                    .collect(Collectors.toList());
        }
        chatJpaList.sort(chatComparator);
        return chatJpaList.subList(pageCapacity * (pageNumber - 1), pageCapacity * pageNumber).stream()
                .map(converter::convertChatJpaToChat)
                .collect(Collectors.toList());
    }
}
