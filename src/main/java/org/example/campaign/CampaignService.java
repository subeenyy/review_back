package org.example.campaign;

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
// import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final PlatformRepository platformRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CampaignMapper campaignMapper;
    // private final Optional<KafkaTemplate<String, ReviewSubmittedEvent>>
    // kafkaTemplate;

    public CampaignService(CampaignRepository campaignRepository,
            PlatformRepository platformRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            CampaignMapper campaignMapper) {
        this.campaignRepository = campaignRepository;
        this.platformRepository = platformRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.campaignMapper = campaignMapper;
    }

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
        log.info(">>> [DB] Saving new campaign for user: {}", userId);
        return campaignRepository.saveAndFlush(campaign);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "campaigns", key = "'user:' + #userId + ':all'")
    public List<Campaign> findAllByUserId(Long userId) {
        log.info(">>> [CACHE] MISS - Fetching all campaigns from DB for userId={}", userId);
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
        log.info(">>> [DB] Saving campaign status change. campaignId={}, action={}", campaignId, status);
        campaignRepository.saveAndFlush(s);
        log.info(">>> [CACHE] Evicted 'campaigns' cache for changeStatus.");
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

        if (dto.getVisitDate() != null) {
            existing.setVisitDate(dto.getVisitDate());
        }

        campaignMapper.updateFromDto(dto, existing);

        log.info(">>> [DB] Updating campaign details. campaignId={}", campaignId);
        return campaignRepository.saveAndFlush(existing);
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
        log.info(">>> [DB] Saving campaign review. campaignId={}", campaignId);
        campaignRepository.saveAndFlush(campaign);
        log.info(">>> [CACHE] Evicted 'campaigns' cache for submitReview.");

        /*
         * kafkaTemplate.ifPresent(template -> {
         * template.send(
         * "review-submitted",
         * new ReviewSubmittedEvent(campaign.getId(), userId,
         * reviewUrl)).whenComplete((result, ex) -> {
         * if (ex != null) {
         * log.error("** Kafka 전송 실패", ex);
         * } else {
         * log.info(
         * "** Kafka 전송 성공 topic={}, campaignId={}, userId={}, reviewUrl={}",
         * result.getRecordMetadata().topic(),
         * campaign.getId(),
         * userId,
         * reviewUrl);
         * }
         * });
         * });
         */

    }

    @Transactional(readOnly = true)
    @Cacheable(value = "campaigns", key = "'user:' + #userId + ':status:' + (#status != null ? #status.name() : 'all') + ':order:' + #sort.toString()")
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

    @Transactional(readOnly = true)
    public CampaignMonthlyStatisticsResponse getMonthlyStatistics(Long userId, String startMonthStr, String endMonthStr,
            String base, Long categoryId) {
        YearMonth startMonth = YearMonth.parse(startMonthStr);
        YearMonth endMonth = YearMonth.parse(endMonthStr);
        LocalDate start = startMonth.atDay(1);
        LocalDate end = endMonth.atEndOfMonth();

        List<Campaign> campaigns;
        if ("visitDate".equalsIgnoreCase(base)) {
            campaigns = (categoryId == null)
                    ? campaignRepository.findByUserIdAndVisitDateBetween(userId, start, end)
                    : campaignRepository.findByUserIdAndVisitDateBetweenAndCategory(userId, start, end, categoryId);
        } else {
            campaigns = (categoryId == null)
                    ? campaignRepository.findByUserIdAndDeadlineBetween(userId, start, end)
                    : campaignRepository.findByUserIdAndDeadlineBetweenAndCategory(userId, start, end, categoryId);
        }

        // Group by month
        Map<String, List<Campaign>> grouped = campaigns.stream()
                .collect(Collectors.groupingBy(c -> {
                    LocalDate date = "visitDate".equalsIgnoreCase(base) ? c.getVisitDate() : c.getDeadline();
                    return YearMonth.from(date).toString();
                }));

        List<CampaignMonthlyStatisticsResponse.MonthlyMetrics> monthlyData = new ArrayList<>();
        Map<String, Long> overallStatusDistribution = new HashMap<>();

        // Fill all months in range
        YearMonth current = startMonth;
        while (!current.isAfter(endMonth)) {
            String monthLabel = current.toString();
            List<Campaign> monthCampaigns = grouped.getOrDefault(monthLabel, Collections.emptyList());

            CampaignMonthlyStatisticsResponse.MonthlyMetrics metrics = calculateMetrics(monthLabel, monthCampaigns);
            monthlyData.add(metrics);

            // Accumulate overall status
            monthCampaigns.forEach(c -> overallStatusDistribution.merge(c.getStatus().name(), 1L, (v1, v2) -> v1 + v2));

            current = current.plusMonths(1);
        }

        return CampaignMonthlyStatisticsResponse.builder()
                .monthlyData(monthlyData)
                .statusDistribution(overallStatusDistribution)
                .build();
    }

    private CampaignMonthlyStatisticsResponse.MonthlyMetrics calculateMetrics(String month, List<Campaign> campaigns) {
        long totalCount = campaigns.size();
        Map<String, Long> statusCount = campaigns.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));

        long reserved = statusCount.getOrDefault(Status.RESERVED.name(), 0L)
                + statusCount.getOrDefault(Status.VISITED.name(), 0L)
                + statusCount.getOrDefault(Status.DONE.name(), 0L);
        long visited = statusCount.getOrDefault(Status.VISITED.name(), 0L)
                + statusCount.getOrDefault(Status.DONE.name(), 0L);
        double visitRate = reserved == 0 ? 0 : (double) visited / reserved;

        long done = statusCount.getOrDefault(Status.DONE.name(), 0L);
        long reviewCount = campaigns.stream()
                .filter(c -> Boolean.TRUE.equals(c.getReceiptReview()) && c.getStatus() == Status.DONE)
                .count();
        double reviewRate = done == 0 ? 0 : (double) reviewCount / done;

        long totalSupport = campaigns.stream().mapToLong(c -> c.getSupportAmount() != null ? c.getSupportAmount() : 0)
                .sum();
        long totalExtra = campaigns.stream().mapToLong(c -> c.getExtraCost() != null ? c.getExtraCost() : 0).sum();
        long totalExp = totalSupport + totalExtra;
        double avgExp = totalCount == 0 ? 0 : (double) totalExp / totalCount;

        return CampaignMonthlyStatisticsResponse.MonthlyMetrics.builder()
                .month(month)
                .totalCount(totalCount)
                .statusCount(statusCount)
                .visitRate(visitRate)
                .reviewCount(reviewCount)
                .reviewRate(reviewRate)
                .totalSupportAmount(totalSupport)
                .totalExtraCost(totalExtra)
                .totalExpenditure(totalExp)
                .averageExpenditure(avgExp)
                .build();
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
