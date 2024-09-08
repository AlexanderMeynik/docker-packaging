package ru.edu.spbstu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import ru.edu.spbstu.request.ChatUpdateRequest;
import ru.edu.spbstu.request.CreateChatRequest;
import ru.edu.spbstu.exception.InvalidRequestParameter;
import ru.edu.spbstu.exception.ResourceNotFound;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.ChatUser;
import ru.edu.spbstu.service.ChatService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ChatControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ChatController chatController;

    @Mock
    private ChatService chatService;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
    }

    @Test
    public void getChats_InvalidLogin() throws Exception {
        String errorMessage = "Invalid login";
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("login", "login");
        params.add("page_number", "1");

        when(chatService.getChats(anyString(), anyInt())).thenThrow(new ResourceNotFound(errorMessage));

        MvcResult result = this.mockMvc.perform(get("/get_chats").params(params))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(chatService, times(1)).getChats(anyString(), anyInt());
        Optional<ResourceNotFound> exception = Optional.ofNullable((ResourceNotFound) result.getResolvedException());
        Assertions.assertTrue(exception.isPresent());
        Assertions.assertEquals(exception.get().getMessage(), errorMessage);
    }

    @Test
    public void getChats_InvalidPageNumber() throws Exception {
        String errorMessage = "Invalid page number";
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("login", "login");
        params.add("page_number", "1");

        when(chatService.getChats(anyString(), anyInt())).thenThrow(new InvalidRequestParameter(errorMessage));

        MvcResult result = this.mockMvc.perform(get("/get_chats").params(params))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(chatService, times(1)).getChats(anyString(), anyInt());
        Optional<InvalidRequestParameter> exception = Optional.ofNullable((InvalidRequestParameter) result.getResolvedException());
        Assertions.assertTrue(exception.isPresent());
        Assertions.assertEquals(exception.get().getMessage(), errorMessage);
    }

    @Test
    public void getChats_SuccessResult() throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("login", "login");
        params.add("page_number", "1");
        List<Chat> chats = new ArrayList<>();
        chats.add(new Chat(1L, "name1"));
        chats.add(new Chat(2L, "name2"));

        when(chatService.getChats(anyString(), anyInt())).thenReturn(chats);

        MvcResult result = this.mockMvc.perform(get("/get_chats").params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        verify(chatService, times(1)).getChats(anyString(), anyInt());
        String expected = new ObjectMapper().writer().writeValueAsString(chats);
        Assertions.assertEquals(expected, result.getResponse().getContentAsString());
    }

    @Test
    public void getChatsBySearch_SuccessResult() throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("login", "login");
        params.add("begin", "any string");
        params.add("page_number", "1");
        Chat chat = new Chat();
        chat.setId(1L);
        chat.setName("name");
        List<Chat> chats = new ArrayList<>();
        chats.add(chat);

        when(chatService.getChatsBySearch(anyString(), anyString(), anyInt())).thenReturn(chats);

        MvcResult result = this.mockMvc.perform(get("/get_chats_by_search").params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        verify(chatService, times(1)).getChatsBySearch(anyString(), anyString(), anyInt());
        String expected = new ObjectMapper().writer().writeValueAsString(chats);
        Assertions.assertEquals(expected, result.getResponse().getContentAsString());
    }

    @Test
    public void deleteChat_SuccessResult() throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", "1");

        this.mockMvc.perform(delete("/delete_chat").params(params))
                .andExpect(status().isOk())
                .andReturn();

        verify(chatService, times(1)).deleteChat(anyLong());
    }

    @Test
    public void getChatMembers_SuccessResult() throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", "1");
        ChatUser user = new ChatUser();
        user.setLogin("login2");
        user.setIs_admin(false);
        List<ChatUser> chatUsers = new ArrayList<>();
        chatUsers.add(new ChatUser());
        chatUsers.add(new ChatUser("login1", true));
        chatUsers.add(user);

        when(chatService.getChatMembers(anyLong())).thenReturn(chatUsers);

        MvcResult result = this.mockMvc.perform(get("/get_chat_members").params(params))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        verify(chatService, times(1)).getChatMembers(anyLong());
        String expected = new ObjectMapper().writer().writeValueAsString(chatUsers);
        Assertions.assertEquals(expected, result.getResponse().getContentAsString());
    }

    @Test
    public void createChat_SuccessResult() throws Exception {
        CreateChatRequest request = new CreateChatRequest();

        this.mockMvc.perform(post("/create_chat")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writer().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        verify(chatService, times(1)).createChat(any());
    }

    @Test
    public void deleteUsersFromChat_SuccessResult() throws Exception {
        ChatUpdateRequest request = new ChatUpdateRequest();

        this.mockMvc.perform(patch("/delete_users_from_chat")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writer().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        verify(chatService, times(1)).deleteUsersFromChat(any());
    }

    @Test
    public void addUsersToChat_SuccessResult() throws Exception {
        ChatUpdateRequest request = new ChatUpdateRequest();

        this.mockMvc.perform(patch("/add_users_to_chat")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writer().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        verify(chatService, times(1)).addUsersToChat(any());
    }

    @Test
    public void makeUsersAdmins_SuccessResult() throws Exception {
        ChatUpdateRequest request = new ChatUpdateRequest();

        this.mockMvc.perform(patch("/make_users_admins")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writer().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        verify(chatService, times(1)).makeUsersAdmins(any());
    }
}
