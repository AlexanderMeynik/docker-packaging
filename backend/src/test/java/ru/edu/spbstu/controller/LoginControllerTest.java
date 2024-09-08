package ru.edu.spbstu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.edu.spbstu.exception.ResourceNotFound;
import ru.edu.spbstu.model.Language;
import ru.edu.spbstu.request.SendTemporaryPasswordRequest;
import ru.edu.spbstu.service.LoginService;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private LoginController loginController;

    @Mock
    private LoginService loginService;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(loginController).build();
    }

    @Test
    void sendTmpPassword_InvalidLogin() throws Exception {
        String bodyText = "login";
        SendTemporaryPasswordRequest request = new SendTemporaryPasswordRequest();
        request.setLogin(bodyText);
        request.setLanguage(Language.ENGLISH);

        doThrow(new ResourceNotFound("")).when(loginService).sendTemporaryPassword(isA(String.class), isA(Language.class));

        mockMvc.perform(MockMvcRequestBuilders.patch("/send-tmp-password")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writer().writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andReturn();

        verify(loginService, times(1)).sendTemporaryPassword(anyString(), any(Language.class));
    }

    @Test
    void sendTmpPassword_ValidLogin() throws Exception {
        String bodyText = "login";
        SendTemporaryPasswordRequest request = new SendTemporaryPasswordRequest();
        request.setLogin(bodyText);
        request.setLanguage(Language.ENGLISH);

        doNothing().when(loginService).sendTemporaryPassword(isA(String.class), isA(Language.class));

        mockMvc.perform(MockMvcRequestBuilders.patch("/send-tmp-password")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(new ObjectMapper().writer().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        verify(loginService, times(1)).sendTemporaryPassword(anyString(), any(Language.class));
    }
}