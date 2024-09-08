package ru.edu.spbstu.dao;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.edu.spbstu.model.jpa.UserJpa;

import java.util.Optional;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void getByLogin() {
        String login = "login";

        Assertions.assertTrue(userRepository.getByLogin(login).isEmpty());

        UserJpa userJpa = new UserJpa();
        userJpa.setLogin(login);
        userJpa.setPassword("password");
        userJpa.setEmail("e@mail.ru");
        userJpa.setImage("image");
        userJpa = userRepository.save(userJpa);

        Optional<UserJpa> byLogin = userRepository.getByLogin(login);
        Assertions.assertTrue(byLogin.isPresent());
        Assertions.assertEquals(userJpa, byLogin.get());

        Assertions.assertTrue(userRepository.getByLogin(null).isEmpty());
    }
}