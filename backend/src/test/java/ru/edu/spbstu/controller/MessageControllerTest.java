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
import ru.edu.spbstu.request.EditMessageRequest;
import ru.edu.spbstu.request.SendMessageRequest;
import ru.edu.spbstu.exception.InvalidRequestParameter;
import ru.edu.spbstu.exception.ResourceNotFound;
import ru.edu.spbstu.model.Message;
import ru.edu.spbstu.service.MessageService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class MessageControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private MessageController messageController;

    @Mock
    private MessageService messageService;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(messageController).build();
    }

    @Test
    public void getMessages_InvalidChatId() throws Exception {
        String errorMessage = "Invalid chat id";
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", "1");
        params.add("page_number", "1");

        when(messageService.getMessages(anyLong(), anyInt())).thenThrow(new ResourceNotFound(errorMessage));

        MvcResult result = this.mockMvc.perform(get("/get_messages").params(params))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(messageService, times(1)).getMessages(anyLong(), anyInt());
        Optional<ResourceNotFound> exception = Optional.ofNullable((ResourceNotFound) result.getResolvedException());
        Assertions.assertTrue(exception.isPresent());
        Assertions.assertEquals(exception.get().getMessage(), errorMessage);
    }

    @Test
    public void getMessages_InvalidPageNumber() throws Exception {
        String errorMessage = "Invalid page number";
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", "1");
        params.add("page_number", "1");

        when(messageService.getMessages(anyLong(), anyInt())).thenThrow(new InvalidRequestParameter(errorMessage));

        MvcResult result = this.mockMvc.perform(get("/get_messages").params(params))
                .andExpect(status().isBadRequest())
                .andReturn();

        verify(messageService, times(1)).getMessages(anyLong(), anyInt());
        Optional<InvalidRequestParameter> exception = Optional.ofNullable((InvalidRequestParameter) result.getResolvedException());
        Assertions.assertTrue(exception.isPresent());
        Assertions.assertEquals(exception.get().getMessage(), errorMessage);
    }

    @Test
    public void getMessages_SuccessResult() throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("chat_id", "1");
        params.add("page_number", "1");
        List<Message> messages = new ArrayList<>();
        messages.add(new Message());
        messages.add(new Message(1L, "login1", "login2", 1L, new Date(), "content", false, false, false));

        when(messageService.getMessages(anyLong(), anyInt())).thenReturn(messages);

        MvcResult result = this.mockMvc.perform(get("/get_messages").params(params))
                .andExpect(status().isOk())
                .andReturn();

        verify(messageService, times(1)).getMessages(anyLong(), anyInt());
        String expected = new ObjectMapper().writer().writeValueAsString(messages);
        Assertions.assertEquals(expected, result.getResponse().getContentAsString());
    }

    @Test
    public void sendMessage_SuccessResult() throws Exception {
        SendMessageRequest request = new SendMessageRequest();

        this.mockMvc.perform(post("/send_message")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writer().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        verify(messageService, times(1)).sendMessage(any());
    }

    @Test
    public void deleteMessage_SuccessResult() throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("message_id", "1");

        this.mockMvc.perform(patch("/delete_message").params(params))
                .andExpect(status().isOk())
                .andReturn();

        verify(messageService, times(1)).deleteMessage(anyLong());
    }

    @Test
    public void forwardMessage_SuccessResult() throws Exception {
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("message_id", "1");
        params.add("sender_login", "login");
        params.add("chat_id", "2");

        this.mockMvc.perform(post("/forward_message").params(params))
                .andExpect(status().isOk())
                .andReturn();

        verify(messageService, times(1)).forwardMessage(anyLong(), anyString(), anyLong());
    }

    @Test
    public void editMessage_SuccessResult() throws Exception {
        EditMessageRequest request = new EditMessageRequest();

        this.mockMvc.perform(patch("/edit_message")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writer().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        verify(messageService, times(1)).editMessage(any());
    }
}
