package ru.edu.spbstu.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.util.*;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @InjectMocks
    private MessageService messageService;

    @Mock
    private JpaToModelConverter converter;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private MessageRepository messageRepository;

    @Test
    public void getMessages_InvalidChatId() {
        Long chatId = 1L;
        Integer pageNumber = 1;

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> messageService.getMessages(chatId, pageNumber));
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, never()).getMessagesByChatId(anyLong());
    }

    @Test
    public void getMessages_NoMessages() {
        Long chatId = 1L;
        Integer pageNumber = 1;

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));
        given(messageRepository.getMessagesByChatId(anyLong())).willReturn(Collections.emptyList());

        Assertions.assertThrows(InvalidRequestParameter.class, () -> messageService.getMessages(chatId, pageNumber));
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, times(1)).getMessagesByChatId(anyLong());
    }

    @Test
    public void getMessages_NotFullFirstPage() {
        Long chatId = 1L;
        Integer pageNumber = 1;
        List<MessageJpa> messages = new ArrayList<>();
        for (int i = 0; i < 49; i++) {
            messages.add(new MessageJpa());
        }

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));
        given(messageRepository.getMessagesByChatId(anyLong())).willReturn(messages);

        List<Message> result = messageService.getMessages(chatId, pageNumber);

        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, times(1)).getMessagesByChatId(anyLong());
        List<Message> expectedResult = messages.stream().map(converter::convertMessageJpaToMessage).toList();
        Assertions.assertEquals(expectedResult.size(), result.size());
    }

    @Test
    public void getMessages_FullPage() {
        Long chatId = 1L;
        Integer pageNumber = 1;
        List<MessageJpa> messages = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            messages.add(new MessageJpa());
        }

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));
        given(messageRepository.getMessagesByChatId(anyLong())).willReturn(messages);

        List<Message> result = messageService.getMessages(chatId, pageNumber);

        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, times(1)).getMessagesByChatId(anyLong());
        List<Message> expectedResult = messages.stream().map(converter::convertMessageJpaToMessage).toList();
        Assertions.assertEquals(expectedResult.size(), result.size());
    }

    @Test
    public void getMessages_NotFullSecondPage() {
        Long chatId = 1L;
        Integer pageNumber = 2;
        List<MessageJpa> messages = new ArrayList<>();
        for (int i = 1; i <= 51; i++) {
            messages.add(new MessageJpa());
        }

        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));
        given(messageRepository.getMessagesByChatId(anyLong())).willReturn(messages);

        List<Message> result = messageService.getMessages(chatId, pageNumber);

        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, times(1)).getMessagesByChatId(anyLong());
        List<Message> expectedResult = new ArrayList<>();
        expectedResult.add(converter.convertMessageJpaToMessage(messages.get(messages.size() - 1)));
        Assertions.assertEquals(expectedResult.size(), result.size());
    }

    @Test
    public void sendMessage_InvalidAuthorLogin() {
        SendMessageRequest request = new SendMessageRequest();
        request.setAuthor_login("login");

        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> messageService.sendMessage(request));
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, never()).findByIdIs(anyLong());
        verify(messageRepository, never()).save(any());
    }

    @Test
    public void sendMessage_InvalidSenderLogin() {
        SendMessageRequest request = new SendMessageRequest();
        request.setAuthor_login("login1");
        request.setSender_login("login2");

        given(userRepository.getByLogin(request.getAuthor_login())).willReturn(Optional.of(new UserJpa()));
        given(userRepository.getByLogin(request.getSender_login())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> messageService.sendMessage(request));
        verify(userRepository, times(2)).getByLogin(anyString());
        verify(chatRepository, never()).findByIdIs(anyLong());
        verify(messageRepository, never()).save(any());
    }

    @Test
    public void sendMessage_InvalidChatId() {
        SendMessageRequest request = new SendMessageRequest();
        request.setAuthor_login("login1");
        request.setSender_login("login2");
        request.setChat_id(1L);

        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa()));
        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> messageService.sendMessage(request));
        verify(userRepository, times(2)).getByLogin(anyString());
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, never()).save(any());
    }

    @Test
    public void sendMessage_SuccessResult() {
        SendMessageRequest request = new SendMessageRequest();
        request.setAuthor_login("login1");
        request.setSender_login("login2");
        request.setChat_id(1L);
        request.setContent("content");
        UserJpa userJpa = new UserJpa();
        userJpa.setId(1L);
        ChatJpa chatJpa = new ChatJpa(1L, "name");

        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(userJpa));
        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(chatJpa));

        messageService.sendMessage(request);

        verify(userRepository, times(2)).getByLogin(anyString());
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, times(1)).save(any());
        ArgumentCaptor<MessageJpa> captor = ArgumentCaptor.forClass(MessageJpa.class);
        verify(messageRepository).save(captor.capture());
        Assertions.assertEquals(userJpa, captor.getValue().getAuthor());
        Assertions.assertEquals(userJpa, captor.getValue().getSender());
        Assertions.assertEquals(chatJpa, captor.getValue().getChat());
        Assertions.assertEquals(request.getContent(), captor.getValue().getContent());
        Assertions.assertEquals(false, captor.getValue().getIsDeleted());
        Assertions.assertEquals(false, captor.getValue().getIsEdited());
        Assertions.assertEquals(false, captor.getValue().getIsForwarded());
    }

    @Test
    public void deleteMessage_InvalidMessageId() {
        Long messageId = 1L;

        given(messageRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> messageService.deleteMessage(messageId));
        verify(messageRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, never()).deleteMessage(anyLong());
    }

    @Test
    public void deleteMessage_SuccessResult() {
        Long messageId = 1L;
        MessageJpa messageJpa = new MessageJpa(1L, null, "content", false, false, false, null, null, null);

        given(messageRepository.findByIdIs(anyLong())).willReturn(Optional.of(messageJpa));

        messageService.deleteMessage(messageId);
        verify(messageRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, times(1)).deleteMessage(anyLong());
    }

    @Test
    public void forwardMessage_InvalidMessageId() {
        Long messageId = 1L;
        String senderLogin = "login";
        Long chatId = 1L;

        given(messageRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> messageService.forwardMessage(messageId, senderLogin, chatId));
        verify(messageRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, never()).getByLogin(anyString());
        verify(chatRepository, never()).findByIdIs(anyLong());
        verify(messageRepository, never()).save(any());
    }

    @Test
    public void forwardMessage_InvalidSenderLogin() {
        Long messageId = 1L;
        String senderLogin = "login";
        Long chatId = 1L;

        given(messageRepository.findByIdIs(anyLong())).willReturn(Optional.of(new MessageJpa()));
        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> messageService.forwardMessage(messageId, senderLogin, chatId));
        verify(messageRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, never()).findByIdIs(anyLong());
        verify(messageRepository, never()).save(any());
    }

    @Test
    public void forwardMessage_InvalidChatId() {
        Long messageId = 1L;
        String senderLogin = "login";
        Long chatId = 1L;

        given(messageRepository.findByIdIs(anyLong())).willReturn(Optional.of(new MessageJpa()));
        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa()));
        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> messageService.forwardMessage(messageId, senderLogin, chatId));
        verify(messageRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, never()).save(any());
    }

    @Test
    public void forwardMessage_SuccessResult() {
        UserJpa author = new UserJpa();
        author.setLogin("author");
        MessageJpa messageJpa = new MessageJpa();
        messageJpa.setAuthor(author);
        messageJpa.setContent("content");
        Long messageId = 1L;
        String senderLogin = "login";
        Long chatId = 1L;

        given(messageRepository.findByIdIs(anyLong())).willReturn(Optional.of(messageJpa));
        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(new UserJpa()));
        given(chatRepository.findByIdIs(anyLong())).willReturn(Optional.of(new ChatJpa()));

        messageService.forwardMessage(messageId, senderLogin, chatId);
        verify(messageRepository, times(1)).findByIdIs(anyLong());
        verify(userRepository, times(1)).getByLogin(anyString());
        verify(chatRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, times(1)).save(any());
        ArgumentCaptor<MessageJpa> captor = ArgumentCaptor.forClass(MessageJpa.class);
        verify(messageRepository).save(captor.capture());
        Assertions.assertEquals(author, captor.getValue().getAuthor());
        Assertions.assertEquals(messageJpa.getContent(), captor.getValue().getContent());
        Assertions.assertEquals(false, captor.getValue().getIsDeleted());
        Assertions.assertEquals(false, captor.getValue().getIsEdited());
        Assertions.assertEquals(true, captor.getValue().getIsForwarded());
    }

    @Test
    public void editMessage_InvalidMessageId() {
        EditMessageRequest request = new EditMessageRequest();
        request.setMessage_id(1L);
        request.setNew_content("new content");

        given(messageRepository.findByIdIs(anyLong())).willReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFound.class, () -> messageService.editMessage(request));
        verify(messageRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, never()).editMessage(anyLong(), anyString());
    }

    @Test
    public void editMessage_SuccessResult() {
        EditMessageRequest request = new EditMessageRequest();
        request.setMessage_id(1L);
        request.setNew_content("new content");

        given(messageRepository.findByIdIs(anyLong())).willReturn(Optional.of(new MessageJpa()));

        messageService.editMessage(request);
        verify(messageRepository, times(1)).findByIdIs(anyLong());
        verify(messageRepository, times(1)).editMessage(anyLong(), anyString());
    }
}
