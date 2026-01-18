package org.example.campaign;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.auth.JwtTokenProvider;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/campaigns")
@RequiredArgsConstructor
public class CampaignController {

        private final CampaignService campaignService;
        private final JwtTokenProvider jwtTokenProvider;

        @GetMapping("/{campaignId}")
        public CampaignResponseDto getCampaignById(
                        @PathVariable Long campaignId,
                        @RequestHeader("Authorization") String token) {
                Long userId = extractUserId(token);

                Campaign s = campaignService.findByCampaignIdAndUser(campaignId, userId)
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                "해당 건을 찾을 수 없습니다"));

                return CampaignResponseDto.fromEntity(s);
        }

        @PostMapping
        public CampaignResponseDto createCampaign(
                        @RequestHeader("Authorization") String token,
                        @RequestBody @Valid CampaignCreateRequestDto sponsorship) {
                Long userId = extractUserId(token);
                Campaign campaign = campaignService.createCampaign(userId, sponsorship);
                return CampaignResponseDto.fromEntity(campaign);
        }

        @GetMapping
        public List<CampaignResponseDto> getCampaigns(
                        @RequestHeader("Authorization") String token) {
                Long userId = extractUserId(token);
                return campaignService.findAllByUserId(userId)
                                .stream()
                                .map(CampaignResponseDto::fromEntity)
                                .toList();
        }

        @PatchMapping("/{campaignId}")
        public CampaignResponseDto updateCampaign(
                        @PathVariable Long campaignId,
                        @RequestHeader("Authorization") String token,
                        @RequestBody CampaignResponseDto dto) {
                Long userId = extractUserId(token);

                Campaign updated = campaignService.updateCampaign(campaignId, userId, dto);
                return CampaignResponseDto.fromEntity(updated);
        }

        @DeleteMapping("/{campaignId}")
        public void deleteCampaign(
                        @PathVariable Long campaignId,
                        @RequestHeader("Authorization") String token) {
                Long userId = extractUserId(token);
                campaignService.deleteCampaign(campaignId, userId);
        }

        @PatchMapping("/{campaignId}/status/{status}")
        public void changeStatus(
                        @PathVariable Long campaignId,
                        @PathVariable CampaignAction status,
                        @RequestParam(required = false) java.time.LocalDate visitDate,
                        @RequestHeader("Authorization") String token) {
                Long userId = extractUserId(token);
                campaignService.changeStatus(campaignId, userId, status, visitDate);
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

        @GetMapping("/status")
        public List<CampaignResponseDto> getCampaignsByStatus(
                        @RequestHeader("Authorization") String token,
                        @RequestParam(required = false) Status status,
                        @RequestParam(defaultValue = "asc") String order) {
                Long userId = extractUserId(token);

                Sort sort = order.equalsIgnoreCase("desc")
                                ? Sort.by("deadline").descending()
                                : Sort.by("deadline").ascending();

                return campaignService.findCampaigns(userId, status, sort);
        }

}
