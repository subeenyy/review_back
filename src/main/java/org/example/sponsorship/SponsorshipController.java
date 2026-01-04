package org.example.sponsorship;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.auth.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sponsorship")
@RequiredArgsConstructor
public class SponsorshipController {

    private final SponsorshipService sponsorshipService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SponsorshipMapper sponsorshipMapper;

    @GetMapping("/{id}")
    public SponsorshipResponseDto getSponsorshipById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);

        Sponsorship s = sponsorshipService.findByIdAndUser(id, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 건을 찾을 수 없습니다"));;

        return SponsorshipResponseDto.fromEntity(s);
    }


    @PostMapping
    public Sponsorship createSponsorship(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid SponsorshipCreateRequestDto sponsorship
    ) {
        Long userId = extractUserId(token);
        return sponsorshipService.createSponsorship(userId, sponsorship);
    }

    @GetMapping
    public List<SponsorshipResponseDto> getSponsorships(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        return sponsorshipService.findAllByUserId(userId)
                .stream()
                .map(SponsorshipResponseDto::fromEntity)
                .toList();
    }

    @PatchMapping("/{id}/{userId}")
    public SponsorshipResponseDto updateSponsorship(
            @PathVariable Long userId,
            @PathVariable Long id,
            @RequestHeader("Authorization") String token,
            @RequestBody SponsorshipResponseDto dto
    ) {
        Long tokenUserId = extractUserId(token);
        if (!tokenUserId.equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "권한이 없습니다");
        }

        Sponsorship updated = sponsorshipService.updateSponsorship(id,dto);
        return SponsorshipResponseDto.fromEntity(updated);
    }


    @PatchMapping("/{id}/status/{status}")
    public void changeStatus(
            @PathVariable Long id,
            @PathVariable SponsorshipAction status,
            @RequestHeader("Authorization") String token
    ) {
        Long userId = extractUserId(token);
        sponsorshipService.changeStatus(id, userId, status);
    }

    private Long extractUserId(String authHeader) {

        log.info("authHeader: [{}]", authHeader);

        String token = authHeader.replace("Bearer ", "");
        log.info("token: [{}]", token);

        return jwtTokenProvider.getUserId(token);

    }

}

