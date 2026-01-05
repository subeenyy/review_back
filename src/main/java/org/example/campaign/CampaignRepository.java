package org.example.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository
        extends JpaRepository<Campaign, Long> {

    List<Campaign> findByUserId(Long userId);
    Optional<Campaign> findByIdAndUser_Id(Long campaignId, Long userId);
    boolean existsByIdAndUser_Id(Long campaignId, Long userId);
}

