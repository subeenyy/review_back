package org.example.campaign;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.auth.JwtTokenProvider;
import org.example.review.ReviewSubmittedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/campaign")
@RequiredArgsConstructor
public class CampaignController {

    private final CampaignService campaignService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CampaignRepository campaignRepository;

    @GetMapping("/{campaignId}")
    public CampaignResponseDto getCampaignById(
            @PathVariable Long campaignId,
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);

        Campaign s = campaignService.findByCampaignIdAndUser(campaignId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 건을 찾을 수 없습니다"));;

        return CampaignResponseDto.fromEntity(s);
    }


    @PostMapping
    public Campaign createCampaign(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid CampaignCreateRequestDto sponsorship
    ) {
        Long userId = extractUserId(token);
        return campaignService.createCampaign(userId, sponsorship);
    }

    @GetMapping
    public List<CampaignResponseDto> getSponsorships(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        return campaignService.findAllByUserId(userId)
                .stream()
                .map(CampaignResponseDto::fromEntity)
                .toList();
    }

    @PatchMapping("/{campaignId}/{userId}")
    public CampaignResponseDto updateSponsorship(
            @PathVariable Long userId,
            @PathVariable Long campaignId,
            @RequestHeader("Authorization") String token,
            @RequestBody CampaignResponseDto dto
    ) {
        Long tokenUserId = extractUserId(token);
        if (!tokenUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다");
        }

        Campaign updated = campaignService.updateCampaign(campaignId,dto);
        return CampaignResponseDto.fromEntity(updated);
    }


    @PatchMapping("/{campaignId}/status/{status}")
    public void changeStatus(
            @PathVariable Long campaignId,
            @PathVariable CampaignAction status,
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        campaignService.changeStatus(campaignId, userId, status);
    }

    private Long extractUserId(String authHeader) {

        log.info("authHeader: [{}]", authHeader);

        String token = authHeader.replace("Bearer ", "");
        log.info("token: [{}]", token);

        return jwtTokenProvider.getUserId(token);

    }

    @PostMapping("/{campaignId}/review")
    public ResponseEntity<String> submitReview(
            @PathVariable Long campaignId,
            @RequestHeader("Authorization") String token,
            @RequestParam String reviewUrl) {

        Long userId = extractUserId(token);
        campaignService.submitReview(campaignId, userId, reviewUrl);
        return ResponseEntity.ok("Review submitted and reward triggered");
    }

}

