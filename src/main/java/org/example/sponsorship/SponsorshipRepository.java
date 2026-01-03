package org.example.sponsorship;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SponsorshipRepository
        extends JpaRepository<Sponsorship, Long> {

    List<Sponsorship> findByUserId(Long userId);

    Optional<Sponsorship> findByIdAndUserId(Long id, Long userId);
}

