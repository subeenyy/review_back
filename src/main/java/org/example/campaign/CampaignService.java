package org.example.campaign;

import lombok.RequiredArgsConstructor;
import org.example.review.ReviewSubmittedEvent;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final UserRepository userRepository;
    private final CampaignMapper campaignMapper;
    private final KafkaTemplate<String, ReviewSubmittedEvent> kafkaTemplate;


    public Campaign createCampaign(Long userId, CampaignCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Campaign sponsorship = Campaign.create(user, request);

        return campaignRepository.save(sponsorship);
    }


    public List<Campaign> findAllByUserId(Long userId) {
        return campaignRepository.findByUserId(userId);
    }


    public void changeStatus(Long campaignId, Long userId, CampaignAction status) {
        Campaign s = campaignRepository.findByIdAndUser_Id(campaignId, userId)
                .orElseThrow();

        switch (status) {
            case RESERVE -> s.reserve();
            case VISIT  -> s.visit();
            case COMPLETE -> s.complete();
            case CANCEL -> s.cancel();
            default -> throw new IllegalArgumentException("지원 상태로 되돌릴 수 없음");
        }

    }

    @Transactional(readOnly = true)
    public Optional<Campaign> findByCampaignIdAndUser(Long campaignId, Long userId) {
        return campaignRepository.findByIdAndUser_Id(campaignId, userId);
    }

    public Campaign save(Campaign campaign) {
        return campaignRepository.save(campaign);
    }


    public Campaign updateCampaign(Long campaignId, CampaignResponseDto dto) {
        Campaign existing = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        campaignMapper.updateFromDto(dto, existing);

        return campaignRepository.save(existing);
    }

    public void submitReview(Long campaignId, Long userId, String reviewUrl) {

        Campaign campaign = campaignRepository
                .findByIdAndUser_Id(campaignId, userId)
                .orElseThrow(() -> new AccessDeniedException("본인 캠페인이 아닙니다."));

        if (campaign.getStatus() != Status.VISITED) {
            throw new IllegalStateException("방문 완료 상태에서만 리뷰 등록 가능");
        }

        campaign.setReviewUrl(reviewUrl);
        campaign.complete(); // DONE

        kafkaTemplate.send(
                "review-submitted",
                new ReviewSubmittedEvent(campaign.getId(), userId, reviewUrl)
        );
    }


}
