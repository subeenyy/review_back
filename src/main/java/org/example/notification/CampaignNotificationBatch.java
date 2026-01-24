package org.example.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.campaign.Campaign;
import org.example.campaign.CampaignRepository;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CampaignNotificationBatch {

    private final UserRepository userRepository;
    private final CampaignRepository campaignRepository;
    private final EmailService emailService;

    // Run every hour at the top of the hour
    @Scheduled(cron = "0 0 * * * *")
    public void sendDeadlineNotifications() {
        int currentHour = LocalDateTime.now().getHour();
        log.info("Starting deadline notification batch for hour: {}", currentHour);

        List<User> usersToNotify = userRepository.findByNotificationEnabledTrueAndNotificationHour(currentHour);
        if (usersToNotify.isEmpty()) {
            log.info("No users to notify for this hour.");
            return;
        }

        List<Long> userIds = usersToNotify.stream().map(User::getId).collect(Collectors.toList());
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(3);

        List<Campaign> campaigns = campaignRepository.findByUsersAndDeadlineBefore(userIds, threshold);

        Map<User, List<Campaign>> campaignsByUser = campaigns.stream()
                .collect(Collectors.groupingBy(Campaign::getUser));

        for (User user : usersToNotify) {
            List<Campaign> userCampaigns = campaignsByUser.get(user);
            if (userCampaigns != null && !userCampaigns.isEmpty()) {
                sendConsolidatedEmail(user, userCampaigns, today);
            }
        }
    }

    private void sendConsolidatedEmail(User user, List<Campaign> campaigns, LocalDate today) {
        StringBuilder body = new StringBuilder();
        body.append(String.format("안녕하세요, %s님!\n\n", user.getNickname() != null ? user.getNickname() : "회원"));
        body.append("마감이 임박하거나 지연된 캠페인이 있습니다. 아래 내용을 확인해 주세요:\n\n");

        for (Campaign c : campaigns) {
            long daysLeft = ChronoUnit.DAYS.between(today, c.getDeadline());
            String statusLabel;
            if (daysLeft < 0) {
                statusLabel = "[지연됨!!]";
            } else if (daysLeft == 0) {
                statusLabel = "[오늘 마감]";
            } else {
                statusLabel = String.format("[D-%d]", daysLeft);
            }

            body.append(String.format("%s %s (플랫폼: %s)\n",
                    statusLabel, c.getStoreName(), c.getPlatform().getName()));
            body.append(String.format("- 마감일: %s\n", c.getDeadline()));
            body.append(String.format("- 현재 상태: %s\n\n", c.getStatus()));
        }

        body.append("프로젝트 관리를 위해 앱에서 확인 부탁드립니다.\n");
        body.append("감사합니다.");

        emailService.sendEmail(user.getEmail(), "[알림] 캠페인 마감 임박 및 지연 안내", body.toString());
    }
}
