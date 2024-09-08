package ru.edu.spbstu.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.edu.spbstu.request.EditMessageRequest;
import ru.edu.spbstu.request.SendMessageRequest;
import ru.edu.spbstu.model.Message;
import ru.edu.spbstu.service.MessageService;

import java.util.List;

@RestController
@AllArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/get_messages")
    public List<Message> getMessages(@RequestParam("chat_id") Long chatId, @RequestParam("page_number") Integer pageNumber) {
        return messageService.getMessages(chatId, pageNumber);
    }

    @PostMapping("/send_message")
    public void sendMessage(@RequestBody SendMessageRequest request) {
        messageService.sendMessage(request);
    }

    @PatchMapping("/delete_message")
    public void deleteMessage(@RequestParam("message_id") Long messageId) {
        messageService.deleteMessage(messageId);
    }

    @PostMapping("/forward_message")
    public void forwardMessage(@RequestParam("message_id") Long messageId, @RequestParam("sender_login") String senderLogin,
                               @RequestParam("chat_id") Long chatId) {
        messageService.forwardMessage(messageId, senderLogin, chatId);
    }

    @PatchMapping("/edit_message")
    public void editMessage(@RequestBody EditMessageRequest request) {
        messageService.editMessage(request);
    }
}
