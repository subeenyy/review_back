package org.example.sponsorship;

import lombok.RequiredArgsConstructor;
import org.example.user.User;
import org.example.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.example.sponsorship.SponsorshipAction.RESERVE;
import static org.example.sponsorship.SponsorshipAction.VISIT;

@Service
@RequiredArgsConstructor
public class SponsorshipService {

    private final SponsorshipRepository sponsorshipRepository;
    private final UserRepository userRepository;

    @Transactional
    public Sponsorship create(Long userId, SponsorshipCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Sponsorship sponsorship = Sponsorship.create(user, request);

        return sponsorshipRepository.save(sponsorship);
    }



    public List<Sponsorship> findMy(Long userId) {
        return sponsorshipRepository.findByUserId(userId);
    }

    @Transactional
    public void changeStatus(Long id, Long userId, SponsorshipAction action) {
        Sponsorship s = sponsorshipRepository.findByIdAndUserId(id, userId)
                .orElseThrow();

        switch (action) {
            case RESERVE -> s.reserve();
            case VISIT  -> s.visit();
            case COMPLETE -> s.complete();
            case CANCEL -> s.cancel();
            default -> throw new IllegalArgumentException("지원 상태로 되돌릴 수 없음");
        }
    }

}
