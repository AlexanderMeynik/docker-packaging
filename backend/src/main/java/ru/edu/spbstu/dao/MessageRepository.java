package ru.edu.spbstu.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.edu.spbstu.model.jpa.MessageJpa;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends CrudRepository<MessageJpa, Long> {

    Optional<MessageJpa> findByIdIs(Long messageId);

    @Modifying
    void deleteAllByChat_Id(Long chatId);

    @Query("select m from MessageJpa m where m.chat.id = :chatId order by m.date desc")
    List<MessageJpa> getMessagesByChatId(@Param("chatId") Long chatId);

    @Modifying
    @Query("update MessageJpa m set m.isDeleted = true where m.id = :messageId")
    void deleteMessage(@Param("messageId") Long messageId);

    @Modifying
    @Query("update MessageJpa m set m.content = :newContent, m.isEdited = true where m.id = :messageId")
    void editMessage(@Param("messageId") Long messageId, @Param("newContent") String newContent);
}
