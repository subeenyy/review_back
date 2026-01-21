package org.example.campaign;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.category.Category;
import org.example.category.CategoryRepository;
import org.example.platform.Platform;
import org.example.platform.PlatformRepository;
import org.example.review.ReviewSubmittedEvent;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final PlatformRepository platformRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CampaignMapper campaignMapper;
    private final KafkaTemplate<String, ReviewSubmittedEvent> kafkaTemplate;

    @org.springframework.cache.annotation.CacheEvict(value = "campaigns", allEntries = true)
    public Campaign createCampaign(Long userId, CampaignCreateRequestDto request) {
        log.info(">>> [CACHE] Evicting 'campaigns' cache for createCampaign");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Platform platform = platformRepository.findById(request.getPlatformId())
                .orElseThrow(() -> new IllegalArgumentException("플랫폼 없음"));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));

        boolean rewardEnabled = platform.isRewardEnabled();
        Long rewardPolicyId = platform.getRewardPolicyId();

        Campaign campaign = Campaign.create(
                user,
                platform,
                category,
                rewardEnabled,
                rewardPolicyId,
                request);
        return campaignRepository.save(campaign);
    }

    public List<Campaign> findAllByUserId(Long userId) {
        return campaignRepository.findByUserId(userId);
    }

    @org.springframework.cache.annotation.CacheEvict(value = "campaigns", allEntries = true)
    public void changeStatus(Long campaignId, Long userId, CampaignAction status, java.time.LocalDate visitDate) {
        log.info(">>> [CACHE] Evicting 'campaigns' cache for changeStatus. campaignId={}, status={}", campaignId,
                status);
        Campaign s = campaignRepository.findByIdAndUser_Id(campaignId, userId)
                .orElseThrow();

        switch (status) {
            case RESERVE -> {
                if (visitDate == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "방문 예정일은 필수입니다.");
                }
                s.reserve(visitDate);
            }
            case VISIT -> s.visit();
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

    @org.springframework.cache.annotation.CacheEvict(value = "campaigns", allEntries = true)
    public Campaign updateCampaign(Long campaignId, Long userId, CampaignResponseDto dto) {
        log.info(">>> [CACHE] Evicting 'campaigns' cache for updateCampaign. campaignId={}", campaignId);
        Campaign existing = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!existing.getUser().getId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인 캠페인이 아닙니다.");

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));
            existing.setCategory(category);
        }

        if (dto.getPlatformId() != null) {
            Platform platform = platformRepository.findById(dto.getPlatformId())
                    .orElseThrow(() -> new IllegalArgumentException("플랫폼 없음"));
            existing.setPlatform(platform);
        }

        campaignMapper.updateFromDto(dto, existing);

        return campaignRepository.save(existing);
    }

    @org.springframework.cache.annotation.CacheEvict(value = "campaigns", allEntries = true)
    public void submitReview(Long campaignId, Long userId, String reviewUrl) {
        log.info(">>> [CACHE] Evicting 'campaigns' cache for submitReview. campaignId={}", campaignId);

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
                new ReviewSubmittedEvent(campaign.getId(), userId, reviewUrl)).whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("** Kafka 전송 실패", ex);
                    } else {
                        log.info(
                                "** Kafka 전송 성공 topic={}, campaignId={}, userId={}, reviewUrl={}",
                                result.getRecordMetadata().topic(),
                                campaign.getId(),
                                userId,
                                reviewUrl);
                    }
                });

    }

    @Transactional(readOnly = true)
    @Cacheable(value = "campaigns", key = "'user:' + #userId + ':status:' + #status + ':order:' + #sort")
    public List<CampaignResponseDto> findCampaigns(
            Long userId,
            Status status,
            Sort sort) {
        log.info(">>> [CACHE] MISS - Fetching campaigns from DB for userId={}, status={}, sort={}", userId, status,
                sort);
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

    @org.springframework.cache.annotation.CacheEvict(value = "campaigns", allEntries = true)
    public void deleteCampaign(Long campaignId, Long userId) {
        log.info(">>> [CACHE] Evicting 'campaigns' cache for deleteCampaign. campaignId={}", campaignId);
        Campaign campaign = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "캠페인을 찾을 수 없습니다."));

        if (!campaign.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 캠페인만 삭제할 수 있습니다.");
        }

        campaignRepository.delete(campaign);
    }

}
