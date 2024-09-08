package ru.edu.spbstu.dao;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ru.edu.spbstu.model.jpa.ChatJpa;
import ru.edu.spbstu.model.jpa.UserChatDetailsJpa;
import ru.edu.spbstu.model.jpa.UserJpa;

import java.util.List;

@Repository
public interface UserChatDetailsRepository extends CrudRepository<UserChatDetailsJpa, Long> {

    @Modifying
    void deleteAllByChat_Id(Long chatId);

    @Query("select ucd from UserChatDetailsJpa ucd where ucd.chat.id = :chatId")
    List<UserChatDetailsJpa> getChatMembers(@Param("chatId") Long chatId);

    @Modifying
    void deleteByChatAndUser(ChatJpa chat, UserJpa user);

    @Modifying
    @Query("update UserChatDetailsJpa ucd set ucd.chatRole = 'ADMIN' where ucd.chat.id = :chatId AND ucd.user.id = :userId")
    void makeUsersAdmins(@Param("chatId") Long chatId, @Param("userId") Long userId);
}
