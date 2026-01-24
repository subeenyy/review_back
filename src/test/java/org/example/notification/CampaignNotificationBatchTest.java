package org.example.notification;

import org.example.campaign.Campaign;
import org.example.campaign.CampaignRepository;
import org.example.campaign.Status;
import org.example.platform.Platform;
import org.example.user.User;
import org.example.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignNotificationBatchTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private CampaignNotificationBatch notificationBatch;

    private User testUser;
    private Platform testPlatform;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("Tester")
                .notificationEnabled(true)
                .notificationHour(9)
                .build();

        testPlatform = Platform.builder()
                .platformId(1L)
                .name("Naver")
                .build();
    }

    @Test
    @DisplayName("알림 대상 사용자에게 지연 및 임박 메일 발송 성공")
    void sendDeadlineNotifications_Success() {
        // Given
        given(userRepository.findByNotificationEnabledTrueAndNotificationHour(anyInt()))
                .willReturn(List.of(testUser));

        Campaign overdueCampaign = Campaign.builder()
                .id(101L)
                .user(testUser)
                .storeName("Overdue Store")
                .platform(testPlatform)
                .deadline(LocalDate.now().minusDays(1))
                .status(Status.PENDING)
                .build();

        Campaign dDayCampaign = Campaign.builder()
                .id(102L)
                .user(testUser)
                .storeName("D-Day Store")
                .platform(testPlatform)
                .deadline(LocalDate.now())
                .status(Status.RESERVED)
                .build();

        given(campaignRepository.findByUsersAndDeadlineBefore(anyList(), any(LocalDate.class)))
                .willReturn(List.of(overdueCampaign, dDayCampaign));

        // When
        notificationBatch.sendDeadlineNotifications();

        // Then
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendEmail(eq("test@example.com"), anyString(), bodyCaptor.capture());

        String emailBody = bodyCaptor.getValue();
        assertThat(emailBody).contains("[지연됨!!] Overdue Store");
        assertThat(emailBody).contains("[오늘 마감] D-Day Store");
        assertThat(emailBody).contains("Tester님");
    }

    @Test
    @DisplayName("알림 대상 사용자가 없을 경우 중단")
    void sendDeadlineNotifications_NoUsers() {
        // Given
        given(userRepository.findByNotificationEnabledTrueAndNotificationHour(anyInt()))
                .willReturn(Collections.emptyList());

        // When
        notificationBatch.sendDeadlineNotifications();

        // Then
        verify(campaignRepository, never()).findByUsersAndDeadlineBefore(anyList(), any());
        verify(emailService, never()).sendEmail(anyString(), anyString(), anyString());
    }
}
