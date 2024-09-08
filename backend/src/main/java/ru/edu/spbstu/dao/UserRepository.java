package ru.edu.spbstu.dao;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ru.edu.spbstu.model.jpa.UserJpa;

@Repository
public interface UserRepository extends CrudRepository<UserJpa, Long> {

    Optional<UserJpa> getByLogin(String login);

    Optional<UserJpa> getByEmail(String email);
}
