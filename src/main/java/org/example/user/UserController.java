package org.example.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/settings/notification")
    public NotificationSettingsDto getNotificationSettings(@AuthenticationPrincipal Jwt jwt) {
        log.info("getNotificationSettings called with JWT subject: {}", jwt != null ? jwt.getSubject() : "null");
        if (jwt == null)
            throw new IllegalArgumentException("Authentication required");
        Long userId = Long.valueOf(jwt.getSubject());
        return userService.getNotificationSettings(userId);
    }

    @PatchMapping("/settings/notification")
    public void updateNotificationSettings(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody NotificationSettingsDto settings) {
        log.info("updateNotificationSettings called with JWT subject: {}", jwt != null ? jwt.getSubject() : "null");
        if (jwt == null)
            throw new IllegalArgumentException("Authentication required");
        Long userId = Long.valueOf(jwt.getSubject());
        userService.updateNotificationSettings(userId, settings);
    }

    @GetMapping("/profile")
    public UserProfileDto getProfile(@AuthenticationPrincipal Jwt jwt) {
        log.info("getProfile called with JWT subject: {}", jwt != null ? jwt.getSubject() : "null");
        if (jwt == null) {
            log.error("JWT is null in getProfile");
            throw new IllegalArgumentException("Authentication required");
        }
        try {
            Long userId = Long.valueOf(jwt.getSubject());
            User user = userService.findById(userId);
            return new UserProfileDto(user.getEmail(), user.getNickname());
        } catch (Exception e) {
            log.error("Error in getProfile: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PatchMapping("/profile")
    public void updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ProfileUpdateRequestDto request) {
        log.info("updateProfile called with JWT subject: {}", jwt != null ? jwt.getSubject() : "null");
        if (jwt == null)
            throw new IllegalArgumentException("Authentication required");
        Long userId = Long.valueOf(jwt.getSubject());
        userService.updateProfile(userId, request);
    }
}
