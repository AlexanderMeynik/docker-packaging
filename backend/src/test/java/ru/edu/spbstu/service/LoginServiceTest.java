package ru.edu.spbstu.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.edu.spbstu.dao.UserRepository;
import ru.edu.spbstu.exception.ResourceNotFound;
import ru.edu.spbstu.model.Language;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @InjectMocks
    private LoginService loginService;

    @Mock
    private UserRepository userRepository;

    @Test
    void sendTemporaryPassword_InvalidLogin() {
        String login = "login";

        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());

        assertThrows(ResourceNotFound.class, () -> loginService.sendTemporaryPassword(login, Language.RUSSIAN));

        verify(userRepository, times(1)).getByLogin(anyString());
    }
}