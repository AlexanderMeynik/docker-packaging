package ru.edu.spbstu.dao;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.edu.spbstu.model.jpa.ChatJpa;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends CrudRepository<ChatJpa, Long> {

    Optional<ChatJpa> findByIdIs(Long id);

    @Query("select ucd.chat from UserChatDetailsJpa ucd where ucd.user.id = :userId")
    List<ChatJpa> getChatsByUserId(@Param("userId") Long userId);

    @Query("select ucd.chat from UserChatDetailsJpa ucd where ucd.user.id = :userId " +
            "and lower(ucd.chat.name) like concat(lower(:begin), '%') order by ucd.chat.name")
    List<ChatJpa> getChatsBySearch(@Param("userId") Long userId, @Param("begin")String begin);
}
