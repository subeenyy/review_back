package org.example.sponsorship;

import lombok.RequiredArgsConstructor;
import org.example.auth.JwtTokenProvider;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sponsorships")
@RequiredArgsConstructor
public class SponsorshipController {

    private final SponsorshipService sponsorshipService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public Sponsorship create(
            @RequestHeader("Authorization") String token,
            @RequestBody SponsorshipCreateRequestDto sponsorship
    ) {
        Long userId = jwtTokenProvider.getUserId(token.replace("Bearer ", ""));
        return sponsorshipService.create(userId, sponsorship);
    }

    @GetMapping
    public List<Sponsorship> myList(
            @RequestHeader("Authorization") String token
    ) {
        Long userId = jwtTokenProvider.getUserId(token.replace("Bearer ", ""));
        return sponsorshipService.findMy(userId);
    }

    @PatchMapping("/{id}/action")
    public void act(
            @PathVariable Long id,
            @RequestParam SponsorshipAction action,
            @RequestHeader("Authorization") String token
    ) {
        Long userId = jwtTokenProvider.getUserId(token.replace("Bearer ", ""));
        sponsorshipService.changeStatus(id, userId, action);
    }

}

