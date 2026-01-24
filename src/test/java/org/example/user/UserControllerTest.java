package org.example.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@WithMockUser
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper = new ObjectMapper();
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("알림 설정 조회 성공")
    void getNotificationSettings_Success() throws Exception {
        NotificationSettingsDto settings = new NotificationSettingsDto(true, 10);
        given(userService.getNotificationSettings(userId)).willReturn(settings);

        mockMvc.perform(get("/users/settings/notification")
                .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationEnabled").value(true))
                .andExpect(jsonPath("$.notificationHour").value(10));
    }

    @Test
    @DisplayName("알림 설정 수정 성공")
    void updateNotificationSettings_Success() throws Exception {
        NotificationSettingsDto settings = new NotificationSettingsDto(false, 21);

        mockMvc.perform(patch("/users/settings/notification")
                .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(settings)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("프로필 조회 성공")
    void getProfile_Success() throws Exception {
        UserProfileDto profile = new UserProfileDto("test@example.com", "Tester");
        User user = User.builder().email("test@example.com").nickname("Tester").build();

        given(userService.findById(userId)).willReturn(user);

        mockMvc.perform(get("/users/profile")
                .with(jwt().jwt(jwt -> jwt.subject(userId.toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.nickname").value("Tester"));
    }

    @Test
    @DisplayName("프로필 수정 성공")
    void updateProfile_Success() throws Exception {
        ProfileUpdateRequestDto request = new ProfileUpdateRequestDto("NewNick", "newpass123");

        mockMvc.perform(patch("/users/profile")
                .with(jwt().jwt(jwt -> jwt.subject(userId.toString())))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
