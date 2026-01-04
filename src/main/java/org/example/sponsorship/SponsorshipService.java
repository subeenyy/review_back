package org.example.sponsorship;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
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
public class SponsorshipService {

    private final SponsorshipRepository sponsorshipRepository;
    private final UserRepository userRepository;
    private final SponsorshipMapper sponsorshipMapper;

    public Sponsorship createSponsorship(Long userId, SponsorshipCreateRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 없음"));

        Sponsorship sponsorship = Sponsorship.create(user, request);

        return sponsorshipRepository.save(sponsorship);
    }


    public List<Sponsorship> findAllByUserId(Long userId) {
        return sponsorshipRepository.findByUserId(userId);
    }


    public void changeStatus(Long id, Long userId, SponsorshipAction status) {
        Sponsorship s = sponsorshipRepository.findByIdAndUserId(id, userId)
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
    public Optional<Sponsorship> findByIdAndUser(Long id, Long userId) {
        return sponsorshipRepository.findByIdAndUserId(id, userId);
    }

    public Sponsorship save(Sponsorship sponsorship) {
        return sponsorshipRepository.save(sponsorship);
    }

    @Transactional
    public Sponsorship updateSponsorship(Long id, SponsorshipResponseDto dto) {
        Sponsorship existing = sponsorshipRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        sponsorshipMapper.updateFromDto(dto, existing);

        return sponsorshipRepository.save(existing);
    }

}
