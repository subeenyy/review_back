package org.example.campaign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.platform.Platform;
import org.example.platform.PlatformRepository;
import org.example.review.ReviewSubmittedEvent;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final PlatformRepository platformRepository;
    private final UserRepository userRepository;
    private final CampaignMapper campaignMapper;
    private final KafkaTemplate<String, ReviewSubmittedEvent> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    public Campaign createCampaign(Long userId, CampaignCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Platform platform = platformRepository.findById(request.getPlatformId())
                .orElseThrow(()-> new IllegalArgumentException("플랫폼 없음"));
        
        boolean rewardEnabled = platform.isRewardEnabled();
        Long rewardPolicyId = platform.getRewardPolicyId();

        Campaign campaign = Campaign.create(
                user,
                platform,
                rewardEnabled,
                rewardPolicyId,
                request
        );
        return campaignRepository.save(campaign);
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


    public Campaign updateCampaign(Long campaignId, Long userId, CampaignResponseDto dto) {
        Campaign existing = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if(!existing.getUser().getId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 캠페인이 아닙니다.");

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
        ).whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("** Kafka 전송 실패", ex);
            } else {
                log.info(
                        "** Kafka 전송 성공 topic={}, campaignId={}, userId={}, reviewUrl={}",
                        result.getRecordMetadata().topic(),
                        campaign.getId(),
                        userId,
                        reviewUrl
                );
            }
        });

    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "campaigns",
            key = "'user:' + #userId + ':status:' + #status + ':order:' + #sort"
    )
    public List<CampaignResponseDto> findCampaigns(
            Long userId,
            Status status,
            Sort sort
    ) {
        List<Campaign> campaigns;

        if (status == null) {
            campaigns = campaignRepository.findByUserId(userId);
        } else {
            campaigns = campaignRepository.findByUserIdAndStatus(userId, status, sort);
        }

        return campaigns.stream()
                .map(CampaignResponseDto::fromEntity)
                .toList();
    }


}
