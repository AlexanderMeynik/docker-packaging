package ru.edu.spbstu.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.edu.spbstu.dao.UserRepository;
import ru.edu.spbstu.exception.InvalidRequestParameter;
import ru.edu.spbstu.exception.ResourceNotFound;
import ru.edu.spbstu.exception.UnauthorizedAccess;
import ru.edu.spbstu.model.User;
import ru.edu.spbstu.model.converter.JpaToModelConverter;
import ru.edu.spbstu.model.jpa.UserJpa;
import ru.edu.spbstu.request.EmailUpdateRequest;
import ru.edu.spbstu.request.PasswordUpdateRequest;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {
    @InjectMocks
    private ProfileService profileService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JpaToModelConverter converter;

    @Mock
    private UserDetails userDetails;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_LOGIN = "login";
    private static final String DEFAULT_EMAIL = DEFAULT_LOGIN + "@gmail.com";
    private static final String DEFAULT_NEW_PASSWORD = "newpassword";
    void setupContext() {
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(securityContext.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getUser_InvalidLogin() {
        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFound.class, () -> profileService.getUser(DEFAULT_LOGIN));
        verify(userRepository).getByLogin(DEFAULT_LOGIN);
    }

    @Test
    void getUser_NullLogin() {
        given(userRepository.getByLogin(null)).willReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFound.class, () -> profileService.getUser(null));
        verify(userRepository).getByLogin(null);
    }

    @Test
    void getUser_Successful() {
        UserJpa userJpa = new UserJpa(1L, DEFAULT_LOGIN, DEFAULT_PASSWORD, DEFAULT_EMAIL, null);
        given(userRepository.getByLogin(DEFAULT_LOGIN)).willReturn(Optional.of(userJpa));
        given(converter.convertUserJpaToUser(userJpa)).willReturn(new User(DEFAULT_LOGIN, DEFAULT_PASSWORD, DEFAULT_EMAIL));
        User user = profileService.getUser(DEFAULT_LOGIN);
        Assertions.assertEquals(user.getLogin(), DEFAULT_LOGIN);
        Assertions.assertEquals(user.getPassword(), DEFAULT_PASSWORD);
        Assertions.assertEquals(user.getEmail(), DEFAULT_EMAIL);
        verify(userRepository).getByLogin(DEFAULT_LOGIN);
        verify(converter).convertUserJpaToUser(userJpa);
    }

    @Test
    void updateEmail_InvalidLogin() {
        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());
        setupContext();
        given(userDetails.getUsername()).willReturn(DEFAULT_LOGIN);
        Assertions.assertThrows(ResourceNotFound.class,
                () -> profileService.updateEmail(new EmailUpdateRequest(DEFAULT_LOGIN, DEFAULT_EMAIL)));
        verify(userRepository).getByLogin(DEFAULT_LOGIN);
        verify(userDetails).getUsername();
        verify(authentication).getPrincipal();
        verify(securityContext).getAuthentication();
    }

    @Test
    void updateEmail_NullLogin() {
        setupContext();
        given(userDetails.getUsername()).willReturn(DEFAULT_LOGIN);
        Assertions.assertThrows(UnauthorizedAccess.class,
                () -> profileService.updateEmail(new EmailUpdateRequest(null, DEFAULT_EMAIL)));
        verifyNoInteractions(userRepository);
        verify(userDetails).getUsername();
        verify(authentication).getPrincipal();
        verify(securityContext).getAuthentication();
    }

    @Test
    void updateEmail_DifferentUser() {
        String login1 = "login1";
        String login2 = "login2";
        String email = login1 + "@gmail.com";
        // user 'login2' is logged in
        setupContext();
        given(userDetails.getUsername()).willReturn(login2);
        Assertions.assertThrows(UnauthorizedAccess.class,
                () -> profileService.updateEmail(new EmailUpdateRequest(login1, email)));
        verifyNoInteractions(userRepository);
        verify(userDetails).getUsername();
        verify(authentication).getPrincipal();
        verify(securityContext).getAuthentication();
    }

    @Test
    void updateEmail_Successful() {
        String newEmail = DEFAULT_LOGIN + "new@gmail.com";
        setupContext();
        given(userDetails.getUsername()).willReturn(DEFAULT_LOGIN);
        UserJpa userJpa = new UserJpa(1L, DEFAULT_LOGIN, DEFAULT_PASSWORD, DEFAULT_EMAIL, null);
        given(userRepository.getByLogin(DEFAULT_LOGIN)).willReturn(Optional.of(userJpa));

        profileService.updateEmail(new EmailUpdateRequest(DEFAULT_LOGIN, newEmail));

        verify(userRepository).getByLogin(DEFAULT_LOGIN);
        ArgumentCaptor<UserJpa> captor = ArgumentCaptor.forClass(UserJpa.class);
        verify(userRepository).save(captor.capture());
        Assertions.assertEquals(captor.getValue().getEmail(), newEmail);
        verify(userDetails).getUsername();
        verify(authentication).getPrincipal();
        verify(securityContext).getAuthentication();
    }

    @Test
    void updatePassword_InvalidLogin() {
        String newPassword = "newpassword";
        given(userRepository.getByLogin(anyString())).willReturn(Optional.empty());
        setupContext();
        given(userDetails.getUsername()).willReturn(DEFAULT_LOGIN);
        Assertions.assertThrows(ResourceNotFound.class,
                () -> profileService.updatePassword(new PasswordUpdateRequest(DEFAULT_LOGIN,
                        newPassword, DEFAULT_PASSWORD)));
        verify(userRepository).getByLogin(DEFAULT_LOGIN);
        verify(userDetails).getUsername();
        verify(authentication).getPrincipal();
        verify(securityContext).getAuthentication();
    }

    @Test
    void updatePassword_NullLogin() {
        setupContext();
        given(userDetails.getUsername()).willReturn(DEFAULT_LOGIN);
        Assertions.assertThrows(UnauthorizedAccess.class,
                () -> profileService.updatePassword(new PasswordUpdateRequest(null,
                        DEFAULT_NEW_PASSWORD, DEFAULT_PASSWORD)));
        verifyNoInteractions(userRepository);
        verify(userDetails).getUsername();
        verify(authentication).getPrincipal();
        verify(securityContext).getAuthentication();
    }

    @Test
    void updatePassword_WrongPassword() {
        String wrongPassword = DEFAULT_PASSWORD + "wrong";
        setupContext();
        given(userDetails.getUsername()).willReturn(DEFAULT_LOGIN);
        UserJpa userJpa = new UserJpa(1L, DEFAULT_LOGIN, DEFAULT_PASSWORD, DEFAULT_EMAIL, null);
        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(userJpa));
        given(passwordEncoder.matches(wrongPassword,
                DEFAULT_PASSWORD)).willReturn(false);

        Assertions.assertThrows(InvalidRequestParameter.class,
                () -> profileService.updatePassword(new PasswordUpdateRequest(DEFAULT_LOGIN,
                        DEFAULT_NEW_PASSWORD, wrongPassword)));
        verify(userRepository).getByLogin(DEFAULT_LOGIN);
        verify(userDetails).getUsername();
        verify(authentication).getPrincipal();
        verify(securityContext).getAuthentication();
        verify(passwordEncoder).matches(wrongPassword, DEFAULT_PASSWORD);
    }

    @Test
    void updatePassword_DifferentUser() {
        String differentLogin = "differentLogin";
        // User 'login' is logged in
        setupContext();
        given(userDetails.getUsername()).willReturn(DEFAULT_LOGIN);

        Assertions.assertThrows(UnauthorizedAccess.class,
                () -> profileService.updatePassword(new PasswordUpdateRequest(differentLogin,
                        DEFAULT_NEW_PASSWORD, DEFAULT_PASSWORD)));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
        verify(userDetails).getUsername();
        verify(authentication).getPrincipal();
        verify(securityContext).getAuthentication();

    }

    @Test
    void updatePassword_Successful() {
        String encodedPassword = "encoded";
        setupContext();
        given(userDetails.getUsername()).willReturn(DEFAULT_LOGIN);
        UserJpa userJpa = new UserJpa(1L, DEFAULT_LOGIN, DEFAULT_PASSWORD, DEFAULT_EMAIL, null);
        given(userRepository.getByLogin(anyString())).willReturn(Optional.of(userJpa));
        given(passwordEncoder.matches(DEFAULT_PASSWORD,
                DEFAULT_PASSWORD)).willReturn(true);
        given(passwordEncoder.encode(DEFAULT_NEW_PASSWORD)).willReturn(encodedPassword);

        profileService.updatePassword(new PasswordUpdateRequest(DEFAULT_LOGIN, DEFAULT_NEW_PASSWORD, DEFAULT_PASSWORD));

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).getByLogin(stringArgumentCaptor.capture());
        Assertions.assertEquals(stringArgumentCaptor.getValue(), DEFAULT_LOGIN);
        ArgumentCaptor<UserJpa> captor = ArgumentCaptor.forClass(UserJpa.class);
        verify(userRepository).save(captor.capture());
        Assertions.assertEquals(captor.getValue().getPassword(), encodedPassword);
        verify(userDetails).getUsername();
        verify(authentication).getPrincipal();
        verify(securityContext).getAuthentication();

    }

    @Test
    void getProfilePhoto_InvalidLogin() {
        given(userRepository.getByLogin(DEFAULT_LOGIN)).willReturn(Optional.empty());
        Assertions.assertThrows(ResourceNotFound.class, () -> profileService.getProfilePhoto(DEFAULT_LOGIN));
        verify(userRepository).getByLogin(DEFAULT_LOGIN);
    }

    @Test
    void getProfilePhoto() {
    }

    @Test
    void updateProfilePhoto() {
    }
}