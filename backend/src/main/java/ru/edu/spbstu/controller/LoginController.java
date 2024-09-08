package ru.edu.spbstu.controller;

import lombok.AllArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import ru.edu.spbstu.request.CheckEmailRequest;
import ru.edu.spbstu.request.SendTemporaryPasswordRequest;
import ru.edu.spbstu.request.SignUpRequest;
import ru.edu.spbstu.service.LoginService;

@RestController
@AllArgsConstructor
public class LoginController {
    private final LoginService loginService;

    @PostMapping("/register")
    public void signUp(@RequestBody SignUpRequest request) {
        loginService.signUp(request);
    }

    @PostMapping("/check_user_email")
    public Boolean checkUserEmail(@RequestBody CheckEmailRequest request) {
        return loginService.checkUserEmail(request);
    }

    @PatchMapping("/send-tmp-password")
    public void sendTmpPassword(@RequestBody SendTemporaryPasswordRequest sendTemporaryPasswordRequest) {
        loginService.sendTemporaryPassword(sendTemporaryPasswordRequest.getLogin(),
                sendTemporaryPasswordRequest.getLanguage());
    }

    @GetMapping("/is_user_present")
    public Boolean isUserPresent(@RequestParam String login) {
        return loginService.isUserPresent(login);
    }

    @GetMapping("/is_email_used")
    public Boolean isEmailUsed(@RequestParam String email) {
        return loginService.isEmailUsed(email);
    }

    @GetMapping("/get_login")
    public String getLogin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}
