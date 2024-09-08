package ru.edu.spbstu.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.edu.spbstu.dao.MessageRepository;
import ru.edu.spbstu.model.Language;
import ru.edu.spbstu.request.ChatUpdateRequest;
import ru.edu.spbstu.request.CreateChatRequest;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {

    @InjectMocks
    private ChatService chatService;

    @Mock
    private JpaToModelConverter converter;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserChatDetailsRepository userChatDetailsRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageService messageService;

    @Mock
    private ChatJpaComparator chatComparator;

    @Test
    public void getChats_InvalidLogin() {
        String login = "login";
        Integer pageNumber = 1;

        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.getChats(login, pageNumber));
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, never()).getChatsByUserId(anyLong());
    }

    @Test
    public void getChats_NoChats() {
        String login = "login";
        Integer pageNumber = 1;

        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa(1L, null, null, null, null)));
        given(chatRepository.getChatsByUserId(anyLong())).willReturn(Collections.emptyList());

        Assertions.assertThrows(InvalidRequestParameter.class, () -> chatService.getChats(login, pageNumber));
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, times(1)).getChatsByUserId(anyLong());
    }

    @Test
    public void getChats_NotFullFirstPage() {
        String login = "login";
        Integer pageNumber = 1;
        List<ChatJpa> chats = new ArrayList<>();
        for (int i = 0; i < 19; i++) {
            chats.add(new ChatJpa());
        }

        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa(1L, null, null, null, null)));
        given(chatRepository.getChatsByUserId(anyLong())).willReturn(chats);
        given(chatComparator.compare(any(), any())).willReturn(0);

        List<Chat> result = chatService.getChats(login, pageNumber);

        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, times(1)).getChatsByUserId(anyLong());
        List<Chat> expectedResult = chats.stream().map(converter::convertChatJpaToChat).toList();
        Assertions.assertEquals(expectedResult.size(), result.size());
    }

    @Test
    public void getChats_FullPage() {
        String login = "login";
        Integer pageNumber = 1;
        List<ChatJpa> chats = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            chats.add(new ChatJpa());
        }

        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa(1L, null, null, null, null)));
        given(chatRepository.getChatsByUserId(anyLong())).willReturn(chats);
        given(chatComparator.compare(any(), any())).willReturn(0);

        List<Chat> result = chatService.getChats(login, pageNumber);

        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, times(1)).getChatsByUserId(anyLong());
        List<Chat> expectedResult = chats.stream().map(converter::convertChatJpaToChat).toList();
        Assertions.assertEquals(expectedResult.size(), result.size());
    }

    @Test
    public void getChats_NotFullSecondPage() {
        String login = "login";
        Integer pageNumber = 2;
        List<ChatJpa> chats = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            chats.add(new ChatJpa());
        }

        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa(1L, null, null, null, null)));
        given(chatRepository.getChatsByUserId(anyLong())).willReturn(chats);

        List<Chat> result = chatService.getChats(login, pageNumber);

        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, times(1)).getChatsByUserId(anyLong());
        List<Chat> expectedResult = new ArrayList<>();
        expectedResult.add(converter.convertChatJpaToChat(chats.get(chats.size() - 1)));
        Assertions.assertEquals(expectedResult.size(), result.size());
    }

    @Test
    public void getChatsBySearch_InvalidLogin() {
        String login = "login";
        String search = "any string";
        Integer pageNumber = 1;

        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.getChatsBySearch(login, search, pageNumber));
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, never()).getChatsBySearch(anyLong(), anyString());
    }

    @Test
    public void getChatsBySearch_SuccessResult() {
        String login = "login";
        String search = "any string";
        Integer pageNumber = 1;
        List<ChatJpa> chats = new ArrayList<>();
        chats.add(new ChatJpa(1L, "name"));

        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa(1L, null, null, null, null)));
        given(chatRepository.getChatsBySearch(anyLong(), anyString())).willReturn(chats);

        List<Chat> result = chatService.getChatsBySearch(login, search, pageNumber);

        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, times(1)).getChatsBySearch(anyLong(), anyString());
        List<Chat> expectedResult = chats.stream().map(converter::convertChatJpaToChat).toList();
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void deleteChat_InvalidChatId() {
        Long chatId = 1L;

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.deleteChat(chatId));
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userChatDetailsRepository, never()).deleteAllByChat_Id(anyLong());
        verify(messageRepository, never()).deleteAllByChat_Id(anyLong());
        verify(chatRepository, never()).deleteById(anyLong());
    }

    @Test
    public void deleteChat_SuccessResult() {
        Long chatId = 1L;

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));

        chatService.deleteChat(chatId);

        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userChatDetailsRepository, times(1)).deleteAllByChat_Id(anyLong());
        verify(messageRepository, times(1)).deleteAllByChat_Id(anyLong());
        verify(chatRepository, times(1)).deleteById(anyLong());
    }

    @Test
    public void getChatMembers_InvalidChatId() {
        Long chatId = 1L;

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.getChatMembers(chatId));
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userChatDetailsRepository, never()).getChatMembers(anyLong());
    }

    @Test
    public void getChatMembers_SuccessResult() {
        Long chatId = 1L;
        List<UserChatDetailsJpa> userChatDetails = new ArrayList<>();
        userChatDetails.add(new UserChatDetailsJpa());
        userChatDetails.add(new UserChatDetailsJpa(1L, new UserJpa(), new ChatJpa(), ChatRole.USER));
        userChatDetails.add(new UserChatDetailsJpa(2L, new UserJpa(), new ChatJpa(), ChatRole.ADMIN));

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));
        given(userChatDetailsRepository.getChatMembers(anyLong())).willReturn(userChatDetails);

        List<ChatUser> result = chatService.getChatMembers(chatId);

        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userChatDetailsRepository, times(1)).getChatMembers(anyLong());
        List<ChatUser> expectedResult = userChatDetails.stream().map(converter::convertUserChatDetailsJpaToChatUser).toList();
        Assertions.assertEquals(expectedResult, result);
    }

    @Test
    public void createChat_InvalidLogin() {
        CreateChatRequest request = new CreateChatRequest();
        request.setChat_name("name");
        request.setUser_logins(List.of("login1"));
        request.setAdmin_login("admin");

        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.createChat(request));
        verify(chatRepository, times(1)).save(any());
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(userChatDetailsRepository, never()).save(any());
        verify(messageService, never()).sendMessage(any());
    }

    @Test
    public void createChat_InvalidAdminLogin() {
        CreateChatRequest request = new CreateChatRequest();
        request.setChat_name("name");
        request.setUser_logins(List.of("login1"));
        request.setAdmin_login("admin");

        given(userRepository.getByLogin(request.getUser_logins().get(0))).willReturn(Optional.of(new UserJpa()));
        given(userRepository.getByLogin(request.getAdmin_login())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.createChat(request));
        verify(chatRepository, times(1)).save(any());
        verify(userRepository, times(2)).getByLogin(anyString());
        verify(userChatDetailsRepository, times(1)).save(any());
        verify(messageService, never()).sendMessage(any());
    }

    @Test
    public void createChat_SuccessResult() {
        CreateChatRequest request = new CreateChatRequest();
        request.setChat_name("name");
        request.setUser_logins(List.of("login1", "login2"));
        request.setAdmin_login("admin");
        request.setLanguage(Language.ENGLISH);

        given(chatRepository.save(any())).willReturn(new ChatJpa(1L, "name"));
        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa()));

        chatService.createChat(request);

        verify(chatRepository, times(1)).save(any());
        verify(userRepository, times(3)).getByLogin(anyString());
        verify(userChatDetailsRepository, times(3)).save(any());
        verify(messageService, times(1)).sendMessage(any());
    }

    @Test
    public void deleteUsersFromChat_InvalidChatId() {
        ChatUpdateRequest request = new ChatUpdateRequest();
        request.setChat_id(1L);
        request.setUser_logins(List.of("login1"));

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.deleteUsersFromChat(request));
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, never()).getByLogin(anyString());
        verify(userChatDetailsRepository, never()).deleteByChatAndUser(any(), any());
    }

    @Test
    public void deleteUsersFromChat_InvalidLogin() {
        ChatUpdateRequest request = new ChatUpdateRequest();
        request.setChat_id(1L);
        request.setUser_logins(List.of("login1"));

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));
        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.deleteUsersFromChat(request));
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(userChatDetailsRepository, never()).deleteByChatAndUser(any(), any());
    }

    @Test
    public void deleteUsersFromChat_SuccessResult() {
        ChatUpdateRequest request = new ChatUpdateRequest();
        request.setChat_id(1L);
        request.setUser_logins(List.of("login1", "login2"));

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));
        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa()));

        chatService.deleteUsersFromChat(request);

        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, times(2)).getByLogin(anyString());
        verify(userChatDetailsRepository, times(2)).deleteByChatAndUser(any(), any());
    }

    @Test
    public void addUsersToChat_InvalidChatId() {
        ChatUpdateRequest request = new ChatUpdateRequest();
        request.setChat_id(1L);
        request.setUser_logins(List.of("login1"));

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.addUsersToChat(request));
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, never()).getByLogin(anyString());
        verify(userChatDetailsRepository, never()).save(any());
    }

    @Test
    public void addUsersToChat_InvalidLogin() {
        ChatUpdateRequest request = new ChatUpdateRequest();
        request.setChat_id(1L);
        request.setUser_logins(List.of("login1"));

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));
        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.addUsersToChat(request));
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(userChatDetailsRepository, never()).save(any());
    }

    @Test
    public void addUsersToChat_SuccessResult() {
        ChatUpdateRequest request = new ChatUpdateRequest();
        request.setChat_id(1L);
        request.setUser_logins(List.of("login1", "login2"));

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));
        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa()));

        chatService.addUsersToChat(request);

        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, times(2)).getByLogin(anyString());
        verify(userChatDetailsRepository, times(2)).save(any());
    }

    @Test
    public void makeUsersAdmins_InvalidChatId() {
        ChatUpdateRequest request = new ChatUpdateRequest();
        request.setChat_id(1L);
        request.setUser_logins(List.of("login1"));

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.makeUsersAdmins(request));
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, never()).getByLogin(anyString());
        verify(userChatDetailsRepository, never()).makeUsersAdmins(anyLong(), anyLong());
    }

    @Test
    public void makeUsersAdmins_InvalidLogin() {
        ChatUpdateRequest request = new ChatUpdateRequest();
        request.setChat_id(1L);
        request.setUser_logins(List.of("login1"));

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));
        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> chatService.makeUsersAdmins(request));
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(userChatDetailsRepository, never()).makeUsersAdmins(anyLong(), anyLong());
    }

    @Test
    public void makeUsersAdmins_SuccessResult() {
        ChatUpdateRequest request = new ChatUpdateRequest();
        request.setChat_id(1L);
        request.setUser_logins(List.of("login1", "login2"));
        UserJpa user = new UserJpa();
        user.setId(1L);

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa(1L, "name")));
        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(user));

        chatService.makeUsersAdmins(request);

        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, times(2)).getByLogin(anyString());
        verify(userChatDetailsRepository, times(2)).makeUsersAdmins(anyLong(), anyLong());
    }
}
