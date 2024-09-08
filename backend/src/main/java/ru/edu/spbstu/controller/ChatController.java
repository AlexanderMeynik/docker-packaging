package ru.edu.spbstu.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.edu.spbstu.request.ChatUpdateRequest;
import ru.edu.spbstu.request.CreateChatRequest;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.ChatUser;
import ru.edu.spbstu.service.ChatService;

import java.util.List;

@RestController
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/get_chats")
    public List<Chat> getChats(@RequestParam("login") String login, @RequestParam("page_number") Integer pageNumber) {
        return chatService.getChats(login, pageNumber);
    }

    @GetMapping("/get_chats_by_search")
    public List<Chat> getChatsBySearch(@RequestParam("login") String login,
                                       @RequestParam("begin") String begin,
                                       @RequestParam("page_number") Integer pageNumber) {
        return chatService.getChatsBySearch(login, begin, pageNumber);
    }

    @GetMapping("/get_chat_members")
    public List<ChatUser> getChatMembers(@RequestParam("chat_id") Long chatId) {
        return chatService.getChatMembers(chatId);
    }

    @PostMapping("/create_chat")
    public void createChat(@RequestBody CreateChatRequest request) {
        chatService.createChat(request);
    }

    @DeleteMapping("/delete_chat")
    public void deleteChat(@RequestParam("chat_id") Long chatId) {
        chatService.deleteChat(chatId);
    }

    @PatchMapping("/delete_users_from_chat")
    public void deleteUsersFromChat(@RequestBody ChatUpdateRequest request) {
        chatService.deleteUsersFromChat(request);
    }

    @PatchMapping("/add_users_to_chat")
    public void addUsersToChat(@RequestBody ChatUpdateRequest request) {
        chatService.addUsersToChat(request);
    }

    @PatchMapping("/make_users_admins")
    public void makeUsersAdmins(@RequestBody ChatUpdateRequest request) {
        chatService.makeUsersAdmins(request);
    }
}
