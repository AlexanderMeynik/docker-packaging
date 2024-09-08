package ru.edu.spbstu.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.edu.spbstu.dao.UserRepository;
import ru.edu.spbstu.exception.InvalidRequestParameter;
import ru.edu.spbstu.exception.ResourceNotFound;
import ru.edu.spbstu.exception.UnauthorizedAccess;
import ru.edu.spbstu.model.User;
import ru.edu.spbstu.model.converter.JpaToModelConverter;
import ru.edu.spbstu.model.jpa.UserJpa;
import ru.edu.spbstu.request.EmailUpdateRequest;
import ru.edu.spbstu.request.PasswordUpdateRequest;
import ru.edu.spbstu.request.ProfilePhotoUpdateRequest;

@Service
@AllArgsConstructor
public class ProfileService {
    private final UserRepository userRepository;
    private final JpaToModelConverter converter;

    private final PasswordEncoder passwordEncoder;


    public User getUser(String login) {
        UserJpa userJpa = userRepository.getByLogin(login)
                .orElseThrow(() -> new ResourceNotFound("User with login '" + login + "' was not found"));
        return converter.convertUserJpaToUser(userJpa);
    }

    public void updateEmail(EmailUpdateRequest request) {
        if (!isUsernameMatching(request.getLogin())) {
            throw new UnauthorizedAccess("A user can't edit other users' email");
        }

        UserJpa userJpa = userRepository.getByLogin(request.getLogin())
                .orElseThrow(() -> new ResourceNotFound("User with login '" + request.getLogin() + "' was not found"));
        userJpa.setEmail(request.getNew_email());
        userRepository.save(userJpa);
    }

    public void updatePassword(PasswordUpdateRequest request) {
        if (!isUsernameMatching(request.getLogin())) {
            throw new UnauthorizedAccess("A user can't edit other users' passwords");
        }
        UserJpa userJpa = userRepository.getByLogin(request.getLogin())
                .orElseThrow(() -> new ResourceNotFound("User with login '" + request.getLogin() + "' was not found"));
        if (passwordEncoder.matches(request.getOld_password(),
                userJpa.getPassword())
        ) {
            userJpa.setPassword(passwordEncoder.encode(request.getNew_password()));
            userRepository.save(userJpa);
        } else {
            throw new InvalidRequestParameter("The old password is incorrect");
        }
    }

    public String getProfilePhoto(String login) {
        UserJpa user = userRepository.getByLogin(login)
                .orElseThrow(() -> new ResourceNotFound("User with login '" + login + "' was not found"));
        return user.getImage();
    }

    public void updateProfilePhoto(ProfilePhotoUpdateRequest request) {
        if (!isUsernameMatching(request.getLogin())) {
            throw new UnauthorizedAccess("A user can't edit other users' profile photos");
        }
        UserJpa userJpa = userRepository.getByLogin(request.getLogin())
                .orElseThrow(() -> new ResourceNotFound("User with login '" + request.getLogin() + "' was not found"));
        userJpa.setImage(request.getNew_profile_image());
        userRepository.save(userJpa);
    }

    private Boolean isUsernameMatching(String login) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String username = userDetails.getUsername();
        return username.equals(login);
    }
}
