package ru.edu.spbstu.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.edu.spbstu.model.User;
import ru.edu.spbstu.request.EmailUpdateRequest;
import ru.edu.spbstu.request.PasswordUpdateRequest;
import ru.edu.spbstu.request.ProfilePhotoUpdateRequest;
import ru.edu.spbstu.service.ProfileService;


@RestController
@AllArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/get_profile_photo")
    public String getProfilePhoto(@RequestParam String login) {
        return profileService.getProfilePhoto(login);
    }

    @PostMapping("/update_profile_photo")
    public void updateProfilePhoto(@RequestBody ProfilePhotoUpdateRequest request) {
        profileService.updateProfilePhoto(request);
    }
    @GetMapping("/get_user")
    public User getUser(@RequestParam String login) {
        return profileService.getUser(login);
    }

    @PostMapping("/update_email")
    public void updateEmail(@RequestBody EmailUpdateRequest request) {
        profileService.updateEmail(request);
    }

    @PostMapping("/update_password")
    public void updatePassword(@RequestBody PasswordUpdateRequest request) {
        profileService.updatePassword(request);
    }

}
