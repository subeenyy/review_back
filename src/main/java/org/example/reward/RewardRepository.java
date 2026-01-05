package org.example.reward;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RewardRepository extends JpaRepository<Reward, Long> {
    boolean existsByCampaign_IdAndUser_Id(Long campaignId, Long userId) ;



}
