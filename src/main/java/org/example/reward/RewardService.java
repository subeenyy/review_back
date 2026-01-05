package org.example.reward;

import org.example.campaign.CampaignRepository;
import org.example.review.ReviewSubmittedEvent;
import org.example.user.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class RewardService {

    private final RewardRepository rewardRepository;
    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;

    public RewardService(RewardRepository rewardRepository,
                         CampaignRepository campaignRepository,
                         UserRepository userRepository) {
        this.rewardRepository = rewardRepository;
        this.campaignRepository = campaignRepository;
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = "review-submitted-topic")
    public void handleReviewEvent(ReviewSubmittedEvent event) {
        // 이미 지급 여부 확인
        if(rewardRepository.existsByCampaign_IdAndUser_Id(event.getCampaignId(), event.getUserId())) return;

        Reward reward = new Reward();
        reward.setCampaign(campaignRepository.findById(event.getCampaignId()).get());
        reward.setUser(userRepository.findById(event.getUserId()).get());
        reward.setAmount(3000L); // 예시
        reward.setIssuedAt(java.time.LocalDateTime.now());
        rewardRepository.save(reward);
    }
}
