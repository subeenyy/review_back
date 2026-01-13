package org.example.campaign;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository
        extends JpaRepository<Campaign, Long> {

    @Query("""
        select c from Campaign c
        join fetch c.platform
        where c.user.id = :userId
    """)
    List<Campaign> findByUserId(Long userId);
    Optional<Campaign> findByIdAndUser_Id(Long campaignId, Long userId);
    boolean existsByIdAndUser_Id(Long campaignId, Long userId);
}

