package org.example.campaign;

import lombok.RequiredArgsConstructor;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.http.HttpStatus;
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
        Campaign s = campaignRepository.findByCampaignIdAndUserId(campaignId, userId)
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
        return campaignRepository.findByCampaignIdAndUserId(campaignId, userId);
    }

    public Campaign save(Campaign campaign) {
        return campaignRepository.save(campaign);
    }

    @Transactional
    public Campaign updateCampaign(Long campaignId, CampaignResponseDto dto) {
        Campaign existing = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        campaignMapper.updateFromDto(dto, existing);

        return campaignRepository.save(existing);
    }

}
